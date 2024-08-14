package com.usbank.paypros.dev.messaging.cases;

import com.usbank.paypros.dev.messaging.cases.management.CaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CaseController {

    private final CaseRepository caseRepository;

    public CaseController(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    @GetMapping(value = "/cases")
    public ResponseEntity<?> getAllCases() {
        return new ResponseEntity<>(caseRepository.findAll(), HttpStatus.OK);
    }
}
