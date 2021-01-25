package uk.gov.digital.ho.hocs.workflow.processes;

import java.util.Arrays;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.mockito.ProcessExpressions;
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
import uk.gov.digital.ho.hocs.workflow.util.CallActivityReturnVariable;
import uk.gov.digital.ho.hocs.workflow.util.ExecutionVariableSequence;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {"processes/MPAM_TRIAGE.bpmn"})
public class MPAMTriage {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.2)
            .build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();
    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario processScenario;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void whenReferenceTypeChangeReturnsForward_thenProcessEnds() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "UpdateRefType",
                        "RefType", "Ministerial")));

        ProcessExpressions.registerCallActivityMock("MpamChangeReferenceType")
            .onExecutionDo(new ExecutionVariableSequence(
                Arrays.asList(
                    Arrays.asList(
                        new CallActivityReturnVariable("DIRECTION", "FORWARD"))
                )
            ))
            .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("MPAM_TRIAGE")
                .execute();

        verify(processScenario).hasFinished("EndEvent_MpamTriage");
    }

    @Test
    public void whenReferenceTypeChangeReturnsBackward_thenShowValidateUserInputAgain() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "UpdateRefType")))
            .thenReturn(task -> task.complete(withVariables(
                "valid", true,
                "DIRECTION", "FORWARD",
                "TriageOutcome", "SendToDraft")));
        ProcessExpressions.registerCallActivityMock("MpamChangeReferenceType")
            .onExecutionDo(new ExecutionVariableSequence(
                Arrays.asList(
                    Arrays.asList(
                        new CallActivityReturnVariable("DIRECTION", "BACKWARD"))
                )
            ))
            .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("MPAM_TRIAGE")
                .execute();

        verify(processScenario, times(2)).hasFinished("Validate_UserInput");
        verify(processScenario).hasFinished("EndEvent_MpamTriage");
    }

    @Test
    public void whenSendToDraft_thenUpdatesTeam_andClearsRejected() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "TriageOutcome", "SendToDraft")));

        Scenario.run(processScenario)
                .startByKey("MPAM_TRIAGE")
                .execute();

        verify(processScenario).hasCompleted("Service_UpdateTeamForDraft");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_DRAFT"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasCompleted("Service_ClearRejected");
        verify(bpmnService).blankCaseValues(any(), any(), eq("Rejected"));
        verify(processScenario).hasFinished("EndEvent_MpamTriage");
    }
}
