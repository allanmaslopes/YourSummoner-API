package com.your.summoner.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** For /admin/**: source address allow-list (403), then admin switched off (503). Uses the socket address only. */
class AdminGuardFilter extends OncePerRequestFilter {
    private final RequestMatcher adminPaths = PathPatternRequestMatcher.withDefaults().matcher("/admin/**");
    private final List<IpAddressMatcher> allowed;
    private final boolean enabled;

    AdminGuardFilter(List<String> allowedCidrs, boolean enabled) {
        this.allowed = allowedCidrs.stream().map(String::trim).filter(s -> !s.isEmpty())
                .map(IpAddressMatcher::new).toList();
        this.enabled = enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!adminPaths.matches(request)) {
            chain.doFilter(request, response);
        } else if (!isAllowed(request.getRemoteAddr())) {
            SecurityProblems.write(response, HttpStatus.FORBIDDEN, "Access denied");
        } else if (!enabled) {
            SecurityProblems.write(response, HttpStatus.SERVICE_UNAVAILABLE, "Administration is disabled");
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isAllowed(String address) {
        try {
            return allowed.stream().anyMatch(m -> m.matches(address));
        } catch (RuntimeException e) {
            return false;
        }
    }
}
