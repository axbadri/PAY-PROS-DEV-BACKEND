package com.bigobrains.paypros.dev.messaging.cases.management;

import com.bigobrains.paypros.dev.messaging.cases.Case;

import java.util.List;

public interface CaseRepository {

    void save(Case newCase);
    List<Case> findAll();
    Case findById(String id);
}
