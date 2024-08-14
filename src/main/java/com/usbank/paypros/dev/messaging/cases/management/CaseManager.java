package com.usbank.paypros.dev.messaging.cases.management;

import com.usbank.paypros.dev.messaging.cases.Case;
import org.springframework.stereotype.Service;

@Service
public interface CaseManager<T> {

    void evaluate(Case newCase);
    Case newCase(T t);
}
