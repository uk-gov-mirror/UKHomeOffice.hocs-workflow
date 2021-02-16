package uk.gov.digital.ho.hocs.workflow.processes;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/DCU_MIN.bpmn",
        "processes/DCU_BASE_DATA_INPUT.bpmn",
        "processes/DCU_MIN_MARKUP.bpmn",
        "processes/DCU_MIN_INITIAL_DRAFT.bpmn",
        "processes/DCU_BASE_QA_RESPONSE.bpmn",
        "processes/DCU_MIN_MINISTER_SIGN_OFF.bpmn",
        "processes/DCU_MIN_PRIVATE_OFFICE.bpmn",
        "processes/DCU_BASE_DISPATCH.bpmn",
        "processes/STAGE.bpmn",
        "processes/STAGE_WITH_USER.bpmn"})
public class DCU_MIN {

    public static final String DISPATCH = "CallActivity_1rowgu5";
    public static final String PO_SIGN_OFF = "POSignOff_CallActivity";
    public static final String INITIAL_DRAFT = "InitialDraft_CallActivity";
    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .build();

    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario dcuMinProcess;

    @Before
    public void defaultScenario() {

        Mocks.register("bpmnService", bpmnService);

        whenAtCallActivity("DCU_BASE_DATA_INPUT")
                .alwaysReturn("CopyNumberTen", "FALSE")
                .deploy(rule);
        whenAtCallActivity("DCU_MIN_MARKUP")
                .alwaysReturn("MarkupDecision", "PR")
                .deploy(rule);
        whenAtCallActivity("DCU_MIN_INITIAL_DRAFT")
                .alwaysReturn("InitialDraftDecision", "ACCEPT", "OfflineQA", "FALSE", "ResponseChannel", "EMAIL")
                .deploy(rule);
        whenAtCallActivity("DCU_BASE_QA_RESPONSE")
                .alwaysReturn("QAResponseDecision", "ACCEPT")
                .deploy(rule);
        whenAtCallActivity("DCU_MIN_PRIVATE_OFFICE")
                .alwaysReturn("PrivateOfficeDecision", "ACCEPT")
                .deploy(rule);
        whenAtCallActivity("DCU_BASE_DISPATCH")
                .alwaysReturn("DispatchDecision", "ACCEPT")
                .deploy(rule);
    }

    @Test
    public void acceptMinisterSignOffScenario() {

        whenAtCallActivity("DCU_MIN_MINISTER_SIGN_OFF")
                .thenReturn("MinisterSignOffDecision", "ACCEPT")
                .deploy(rule);

        Scenario.run(dcuMinProcess)
                .startByKey("MIN")
                .execute();

        verify(dcuMinProcess, times(1))
                .hasCompleted(INITIAL_DRAFT);

        verify(dcuMinProcess, times(1))
                .hasCompleted(DISPATCH);

    }

    @Test
    public void rejectMinisterSignOffScenario() {

        whenAtCallActivity("DCU_MIN_MINISTER_SIGN_OFF")
                .thenReturn("MinisterSignOffDecision", "REJECT")
                .thenReturn("MinisterSignOffDecision", "ACCEPT")
                .deploy(rule);

        Scenario.run(dcuMinProcess)
                .startByKey("MIN")
                .execute();

        verify(dcuMinProcess, times(2))
                .hasCompleted(INITIAL_DRAFT);

        verify(dcuMinProcess, times(2))
                .hasCompleted(PO_SIGN_OFF);

        verify(dcuMinProcess, times(1))
                .hasCompleted(DISPATCH);
    }

    @Test
    public void notApplicableMinisterSignOffScenario() {

        whenAtCallActivity("DCU_MIN_MINISTER_SIGN_OFF")
                .thenReturn("MinisterSignOffDecision", "NOT_APPLICABLE")
                .thenReturn("MinisterSignOffDecision", "ACCEPT")
                .deploy(rule);

        Scenario.run(dcuMinProcess)
                .startByKey("MIN")
                .execute();

        verify(dcuMinProcess, times(1))
                .hasCompleted(INITIAL_DRAFT);

        verify(dcuMinProcess, times(2))
                .hasCompleted(PO_SIGN_OFF);

        verify(dcuMinProcess, times(1))
                .hasCompleted(DISPATCH);
    }
}
