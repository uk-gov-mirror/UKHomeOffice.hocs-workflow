package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseworkCaseRequestTest {

    @Test
    public void getCreateCaseRequest() {

        CaseType caseDataType = CaseType.MIN;
        Map<String, String> data = new HashMap<>();

        CreateCaseworkCaseRequest createCaseRequest = new CreateCaseworkCaseRequest(caseDataType, data);

        assertThat(createCaseRequest.getType()).isEqualTo(caseDataType);
        assertThat(createCaseRequest.getData()).isEqualTo(data);

    }

    @Test
    public void getCreateCaseRequestNull() {

        CreateCaseworkCaseRequest createCaseRequest = new CreateCaseworkCaseRequest(null, null);

        assertThat(createCaseRequest.getType()).isNull();
        assertThat(createCaseRequest.getData()).isNull();

    }
}