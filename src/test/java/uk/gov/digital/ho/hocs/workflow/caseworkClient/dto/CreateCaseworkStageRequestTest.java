package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseworkStageRequestTest {

    @Test
    public void getCreateStageRequest() {

        StageType stageType = StageType.DCU_MIN_MARKUP;
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        CreateCaseworkStageRequest createStageRequest = new CreateCaseworkStageRequest(stageType, teamUUID, userUUID, deadline);

        assertThat(createStageRequest.getType()).isEqualTo(stageType);
        assertThat(createStageRequest.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(createStageRequest.getUserUUID()).isEqualTo(userUUID);
        assertThat(createStageRequest.getDeadline()).isEqualTo(deadline);

    }

    @Test
    public void getCreateStageRequestNull() {

        CreateCaseworkStageRequest createStageRequest = new CreateCaseworkStageRequest(null, null, null, null);

        assertThat(createStageRequest.getType()).isNull();
        assertThat(createStageRequest.getTeamUUID()).isNull();
        assertThat(createStageRequest.getUserUUID()).isNull();
        assertThat(createStageRequest.getDeadline()).isNull();

    }

}