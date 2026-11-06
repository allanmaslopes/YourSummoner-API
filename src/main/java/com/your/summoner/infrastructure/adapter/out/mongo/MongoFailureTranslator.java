package com.your.summoner.infrastructure.adapter.out.mongo;

import com.your.summoner.application.port.out.CatalogStorageUnavailableException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

/** Traduz falhas do Mongo na borda do adapter: a application não conhece {@code org.springframework.dao}. */
@Aspect
@Component
public class MongoFailureTranslator {

    @Around("execution(public * com.your.summoner.infrastructure.adapter.out.mongo.MongoStaticDataRepository.*(..))")
    Object translate(ProceedingJoinPoint call) throws Throwable {
        try {
            return call.proceed();
        } catch (DataAccessException e) {
            throw new CatalogStorageUnavailableException("MongoDB unavailable: " + e.getMessage(), e);
        }
    }
}
