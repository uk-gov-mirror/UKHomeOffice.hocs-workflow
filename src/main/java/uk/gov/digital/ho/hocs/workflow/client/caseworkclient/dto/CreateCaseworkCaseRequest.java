package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.Map;

@AllArgsConstructor
@Getter
public class CreateCaseworkCaseRequest {

    @JsonProperty("type")
    private CaseDataType type;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("caseDeadline")
    private LocalDate caseDeadline;
}
