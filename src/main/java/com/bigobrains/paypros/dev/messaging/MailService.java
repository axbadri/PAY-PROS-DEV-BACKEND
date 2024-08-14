package com.bigobrains.paypros.dev.messaging;

import com.bigobrains.paypros.dev.messaging.cases.Case;
import com.bigobrains.paypros.dev.messaging.cases.management.CaseManager;
import com.bigobrains.paypros.dev.messaging.cases.management.CaseRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final CaseRepository caseRepository;
    private final CaseManager<MimeMessage> caseManager;

    public MailService(CaseRepository caseRepository, CaseManager<MimeMessage> caseManager) {
        this.caseRepository = caseRepository;
        this.caseManager = caseManager;
    }

    private static final Logger LOG = LoggerFactory.getLogger(MailService.class);

    public void handleMail(MimeMessage message) {

        Case newCase = caseManager.newCase(message);
        if (newCase != null) {
            LOG.info("Case: [{}]", newCase);
            caseRepository.save(newCase);
            caseManager.evaluate(newCase);
        }
    }
}
