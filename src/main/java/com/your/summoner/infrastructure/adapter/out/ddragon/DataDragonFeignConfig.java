package com.your.summoner.infrastructure.adapter.out.ddragon;

import com.your.summoner.domain.exception.ExternalBadResponseException;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import com.your.summoner.infrastructure.adapter.out.UpstreamNotFoundException;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/** Configuração isolada do client do Data Dragon. */
public class DataDragonFeignConfig {

    @Bean
    Request.Options dataDragonRequestOptions() {
        return new Request.Options(2, TimeUnit.SECONDS, 8, TimeUnit.SECONDS, true);
    }

    @Bean
    ErrorDecoder dataDragonErrorDecoder() {
        return (methodKey, response) -> {
            int status = response.status();
            if (status == 403 || status == 404) {
                return new UpstreamNotFoundException("Data Dragon resource not found");
            }
            String message = "Data Dragon answered " + status;
            return status >= 500
                    ? new ExternalServiceUnavailableException(DataDragonApi.SERVICE, message)
                    : new ExternalBadResponseException(DataDragonApi.SERVICE, message);
        };
    }
}
