package com.your.summoner.infrastructure.adapter.out.cdragon;

import com.your.summoner.domain.exception.ExternalBadResponseException;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/** Configuração isolada do client do Community Dragon. */
public class CommunityDragonFeignConfig {

    @Bean
    Request.Options communityDragonRequestOptions() {
        return new Request.Options(2, TimeUnit.SECONDS, 8, TimeUnit.SECONDS, true);
    }

    @Bean
    ErrorDecoder communityDragonErrorDecoder() {
        return (methodKey, response) -> {
            int status = response.status();
            String message = "Community Dragon answered " + status;
            return status >= 500
                    ? new ExternalServiceUnavailableException(CommunityDragonApi.SERVICE, message)
                    : new ExternalBadResponseException(CommunityDragonApi.SERVICE, message);
        };
    }
}
