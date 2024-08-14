package com.usbank.paypros.dev.messaging.cases;

import org.apache.commons.codec.binary.Base64;

public record Case(String caseID, String createdBy, String subject, String data, String status, String summary) {

    public enum Status {
        ACKNOWLEDGED,
        HOLD,
        ACCEPTED,
        REVIEWED,
        PROCESSED,
        FAILED
    }

    @Override
    public String toString() {
        return  "\n" +
                "caseID=" + caseID + "\n" +
                "createdBy=" + createdBy + "\n" +
                "subject=" + subject + "\n" +
                "data=" + new String(Base64.decodeBase64(data)) + "\n" +
                "status=" + status + "\n" +
                "summary=" + summary;
    }
}
