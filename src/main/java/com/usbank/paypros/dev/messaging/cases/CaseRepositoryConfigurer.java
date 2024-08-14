package com.usbank.paypros.dev.messaging.cases;

import com.usbank.paypros.dev.messaging.cases.management.CaseRepository;
import com.usbank.paypros.dev.messaging.cases.management.InMemoryCaseRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaseRepositoryConfigurer {

    @Bean
    public CaseRepository caseRepository() {
        return new InMemoryCaseRepository();
    }
}
