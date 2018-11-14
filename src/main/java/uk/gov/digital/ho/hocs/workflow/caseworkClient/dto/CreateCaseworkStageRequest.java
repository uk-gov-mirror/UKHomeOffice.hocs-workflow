package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateCaseworkStageRequest {

    @JsonProperty("type")
    private StageType type;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("userUUID")
    private UUID userUUID;

    @JsonProperty("deadline")
    private LocalDate deadline;
}
