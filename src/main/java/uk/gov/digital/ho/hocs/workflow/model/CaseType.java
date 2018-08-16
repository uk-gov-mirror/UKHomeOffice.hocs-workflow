package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

public enum CaseType {
    RSH("WR Response"),
    MIN("Ministerial"),
    TRO("Treat Official"),
    DTEN("Number 10");

    @Getter
    private String displayValue;

    CaseType(String value) {
        displayValue = value;
    }
}

