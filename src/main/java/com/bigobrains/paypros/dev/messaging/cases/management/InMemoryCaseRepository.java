package com.bigobrains.paypros.dev.messaging.cases.management;

import com.bigobrains.paypros.dev.messaging.cases.Case;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InMemoryCaseRepository implements CaseRepository {

    private final Map<String, Case> CASES = new LinkedHashMap<>();

    @Override
    public void save(Case newCase) {
        CASES.put(newCase.caseID(), newCase);
    }

    @Override
    public List<Case> findAll() {
        return CASES.values().stream().toList();
    }

    @Override
    public Case findById(String id) {
        return CASES.entrySet().stream().filter(o -> o.getKey().equals(id)).map(Map.Entry::getValue).findAny().orElse(null);
    }
}
