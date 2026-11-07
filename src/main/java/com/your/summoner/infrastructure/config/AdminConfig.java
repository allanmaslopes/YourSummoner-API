package com.your.summoner.infrastructure.config;

import com.your.summoner.application.port.out.AuditLog;
import com.your.summoner.application.port.out.SecretStore;
import com.your.summoner.application.service.EnvCredentialSettings;
import com.your.summoner.infrastructure.adapter.out.mongo.DisabledSecretStore;
import com.your.summoner.infrastructure.adapter.out.mongo.MongoSecretStore;
import com.your.summoner.infrastructure.security.AdminAuthService;
import com.your.summoner.infrastructure.security.AdminProperties;
import com.your.summoner.infrastructure.security.AesGcmCipher;
import com.your.summoner.infrastructure.security.LoginRateLimiter;
import com.your.summoner.infrastructure.security.MasterKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration(proxyBeanMethods = false)
public class AdminConfig {

    @Bean(destroyMethod = "close")
    ExecutorService adminSettingsExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /** Fails the startup (without printing the key) when the value is set but invalid. */
    @Bean
    MasterKey masterKey(@Value("${yoursummoner.master-key:}") String base64) {
        return MasterKey.parse(base64);
    }

    @Bean
    EnvCredentialSettings envCredentialSettings(RiotApiProperties riot) {
        return new EnvCredentialSettings(riot.token());
    }

    @Bean
    SecretStore secretStore(MasterKey masterKey, MongoTemplate mongo) {
        return masterKey.present() ? new MongoSecretStore(mongo, new AesGcmCipher(masterKey.bytes()))
                : new DisabledSecretStore();
    }

    @Bean
    LoginRateLimiter loginRateLimiter() {
        return new LoginRateLimiter(Clock.systemUTC());
    }

    @Bean
    AdminAuthService adminAuthService(AdminProperties properties, MasterKey masterKey, SecretStore secrets,
                                      AuditLog audit, LoginRateLimiter limiter) {
        return new AdminAuthService(properties, masterKey, secrets, audit, limiter, Clock.systemUTC());
    }
}
