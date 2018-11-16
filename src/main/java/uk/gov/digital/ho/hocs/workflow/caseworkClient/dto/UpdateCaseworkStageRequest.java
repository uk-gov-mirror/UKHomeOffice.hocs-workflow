package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.StageStatusType;

import java.util.UUID;

@AllArgsConstructor
public class UpdateCaseworkStageRequest {

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("userUUID")
    private UUID userUUID;

    @JsonProperty("status")
    private StageStatusType status;

}
