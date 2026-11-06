package com.your.summoner.infrastructure.adapter.out.mongo;

import com.your.summoner.application.port.out.CatalogStorageUnavailableException;
import com.your.summoner.application.port.out.StaticDataRepository;
import com.your.summoner.domain.model.Lane;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MongoFailureTranslatorTest {

    @Test
    void mongoFailuresBecomeCatalogStorageUnavailable() {
        MongoTemplate template = mock(MongoTemplate.class);
        when(template.findOne(any(org.springframework.data.mongodb.core.query.Query.class), any(Class.class)))
                .thenThrow(new DataAccessResourceFailureException("connection refused"));
        var target = new MongoStaticDataRepository(template, mock(CatalogDocumentMapper.class),
                new ConcurrentMapCacheManager());
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new MongoFailureTranslator());
        StaticDataRepository repository = factory.getProxy();

        assertThatThrownBy(() -> repository.findPickRates("14.1.1", Lane.TOP))
                .isInstanceOf(CatalogStorageUnavailableException.class)
                .hasCauseInstanceOf(DataAccessResourceFailureException.class);
    }
}
