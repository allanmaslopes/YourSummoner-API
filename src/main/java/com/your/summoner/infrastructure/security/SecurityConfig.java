package com.your.summoner.infrastructure.security;

import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {
    private static final String[] PUBLIC_READS = {
            "/champions/**", "/matchs/**", "/summoners/**", "/patches/**",
            "/actuator/health", "/actuator/health/**",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**"};

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AdminProperties admin, MasterKey masterKey)
            throws Exception {
        boolean enabled = masterKey.present() && !admin.user().isBlank() && !admin.passwordHash().isBlank();
        AuthenticationEntryPoint unauthorized = (request, response, ex) -> {
            response.setHeader("WWW-Authenticate", "Bearer");
            SecurityProblems.write(response, HttpStatus.UNAUTHORIZED, "Authentication required");
        };
        AccessDeniedHandler forbidden = (request, response, ex) ->
                SecurityProblems.write(response, HttpStatus.FORBIDDEN, "Access denied");

        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(authorities);

        http.csrf(AbstractHttpConfigurer::disable) // Bearer-only API: no cookies, no session
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new AdminGuardFilter(admin.allowedCidrs(), enabled), BearerTokenAuthenticationFilter.class)
                .exceptionHandling(e -> e.authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden))
                .oauth2ResourceServer(o -> o.authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden)
                        .jwt(j -> j.decoder(jwtDecoder(masterKey)).jwtAuthenticationConverter(jwtConverter)))
                .authorizeHttpRequests(a -> {
                    a.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
                    a.requestMatchers(HttpMethod.POST, "/admin/session").permitAll();
                    a.requestMatchers("/admin/**").hasAuthority("ADMIN");
                    for (HttpMethod method : new HttpMethod[]{HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS}) {
                        a.requestMatchers(method, PUBLIC_READS).permitAll();
                    }
                    a.anyRequest().denyAll();
                });
        return http.build();
    }

    private static JwtDecoder jwtDecoder(MasterKey masterKey) {
        if (!masterKey.present()) {
            return token -> {
                throw new BadJwtException("Administration is disabled");
            };
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(new SecretKeySpec(masterKey.derive(AdminAuthService.JWT_INFO), "HmacSHA256")).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(Duration.ZERO)));
        return decoder;
    }

    /** Keeps Boot from generating (and logging) a default user and password. */
    @Bean
    UserDetailsService noUsers() {
        return new InMemoryUserDetailsManager();
    }
}
