package uk.gov.digital.ho.hocs.workflow.processes;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
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

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {"processes/MPAM_CHANGE_REFERENCE_TYPE.bpmn"})
public class MPAM_CHANGE_REFERENCE_TYPE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(1.0)
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

    public static final String BUSINESS_KEY = "myBusinessKey";
    public static final String CASE_ID = "CASE_ID";

    @Test
    public void whenMinisterialToOfficialAndNotCorrection_thenMinisterialValuesAreClearedAndRefTypeUpdateCountIncreased() {

        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToOfficial"))
            .thenReturn(task -> task.complete(withVariables(
                "valid", true,
                "DIRECTION", "FORWARD",
                "CaseNote_TriageChangeCaseType", "noteText",
                "RefTypeCorrection", "XXXX")));

        Scenario.run(processScenario).startBy(() ->
            rule.getRuntimeService().startProcessInstanceByKey(
                "MPAM_CHANGE_REFERENCE_TYPE",
                BUSINESS_KEY,
                Map.of("CaseUUID", "CASE_ID", "RefType", "Ministerial"))
        ).execute();

        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Official"), eq("RefTypeStatus"), eq("Confirm"));
        verify(bpmnService).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
        verify(bpmnService).updateCount(any(),eq("RefTypeUpdateCount"),eq(1));
        verify(bpmnService).createCaseConversionNote(eq(CASE_ID), eq(BUSINESS_KEY), eq("noteText"));
        verify(processScenario).hasCompleted("EndEvent");
    }

    @Test
    public void whenMinisterialToOfficialAndCorrection_thenMinisterialValuesAreCleared_AndRefTypeUpdateCountNotIncreased() {

        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToOfficial"))
            .thenReturn(task -> task.complete(withVariables(
                "valid", true,
                "DIRECTION", "FORWARD",
                "CaseNote_TriageChangeCaseType", "noteText",
                "RefTypeCorrection", "Correction")));

        Scenario.run(processScenario).startBy(() ->
            rule.getRuntimeService().startProcessInstanceByKey(
                "MPAM_CHANGE_REFERENCE_TYPE",
                BUSINESS_KEY,
                Map.of("CaseUUID", "CASE_ID", "RefType", "Ministerial"))
        ).execute();

        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Official"), eq("RefTypeStatus"), eq("Confirm"));
        verify(bpmnService).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
        verify(bpmnService, never()).updateCount(any(), anyString(), anyInt());
        verify(bpmnService).createCaseConversionNote(eq(CASE_ID), eq(BUSINESS_KEY), eq("noteText"));
        verify(processScenario).hasCompleted("EndEvent");
    }

    @Test
    public void whenMinisterialToOfficialAndDirectionIsNotForward_thenItCompletesImmediatelyWithoutMakingAnyChanges() {

        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToOfficial"))
            .thenReturn(task -> task.complete(withVariables(
                "valid", true,
                "DIRECTION", "BACKWARD")));

        Scenario.run(processScenario)
            .startByKey("MPAM_CHANGE_REFERENCE_TYPE", Map.of("RefType", "Ministerial"))
            .execute();

        verify(bpmnService, never()).updateValue(any(), any(), any(), any(), any(), any());
        verify(bpmnService, never()).blankCaseValues(any(), any(), any(), any());
        verify(bpmnService, never()).updateCount(any(), anyString(), anyInt());
        verify(bpmnService, never()).createCaseConversionNote(anyString(), anyString(), anyString());
        verify(processScenario).hasCompleted("EndEvent");
    }

    @Test
    public void whenMinisterialToOfficialAndDirectionIsForwardButInvalid_thenFormIsRepresented() {

        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToOfficial"))
            .thenReturn(task -> task.complete(withVariables(
                "valid", false,
                "DIRECTION", "FORWARD")))
            .thenReturn(task -> task.complete(withVariables(
                "valid", true,
                "DIRECTION", "FORWARD",
                "CaseNote_TriageChangeCaseType", "noteText",
                "RefTypeCorrection", "XXXX"
                )));

        Scenario.run(processScenario).startBy(() ->
            rule.getRuntimeService().startProcessInstanceByKey(
                "MPAM_CHANGE_REFERENCE_TYPE",
                BUSINESS_KEY,
                Map.of("CaseUUID", "CASE_ID", "RefType", "Ministerial"))
        ).execute();

        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Official"), eq("RefTypeStatus"), eq("Confirm"));
        verify(bpmnService).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
        verify(bpmnService).updateCount(any(),eq("RefTypeUpdateCount"),eq(1));
        verify(bpmnService).createCaseConversionNote(eq(CASE_ID), eq(BUSINESS_KEY), eq("noteText"));
        verify(processScenario).hasCompleted("EndEvent");
        verify(processScenario, times(2)).hasFinished("Validate_ReferenceTypeToOfficial");
    }

    ////

    @Test
    public void whenOfficialToMinisterialAndNotCorrection_thenMinisterialValuesAreClearedAndRefTypeUpdateCountIncreased() {

        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToMinisterial"))
            .thenReturn(task -> task.complete(withVariables(
                "valid", true,
                "DIRECTION", "FORWARD",
                "CaseNote_TriageChangeCaseType", "noteText",
                "RefTypeCorrection", "XXXX")));

        Scenario.run(processScenario).startBy(() ->
            rule.getRuntimeService().startProcessInstanceByKey(
                "MPAM_CHANGE_REFERENCE_TYPE",
                BUSINESS_KEY,
                Map.of("CaseUUID", "CASE_ID", "RefType", "Official"))
        ).execute();

        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Ministerial"), eq("RefTypeStatus"), eq("Confirm"));
        verify(bpmnService, never()).blankCaseValues(any(), any(), any(), any());
        verify(bpmnService).updateCount(any(),eq("RefTypeUpdateCount"),eq(1));
        verify(bpmnService).createCaseConversionNote(eq(CASE_ID), eq(BUSINESS_KEY), eq("noteText"));
        verify(processScenario).hasCompleted("EndEvent");
    }

    @Test
    public void whenOfficialToMinisterialAndCorrection_thenMinisterialValuesAreCleared_AndRefTypeUpdateCountNotIncreased() {

        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToMinisterial"))
            .thenReturn(task -> task.complete(withVariables(
                "valid", true,
                "DIRECTION", "FORWARD",
                "CaseNote_TriageChangeCaseType", "noteText",
                "RefTypeCorrection", "Correction")));

        Scenario.run(processScenario).startBy(() ->
            rule.getRuntimeService().startProcessInstanceByKey(
                "MPAM_CHANGE_REFERENCE_TYPE",
                BUSINESS_KEY,
                Map.of("CaseUUID", "CASE_ID", "RefType", "Official"))
        ).execute();

        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Ministerial"), eq("RefTypeStatus"), eq("Confirm"));
        verify(bpmnService, never()).blankCaseValues(any(), any(), any(), any());
        verify(bpmnService, never()).updateCount(any(), anyString(), anyInt());
        verify(bpmnService).createCaseConversionNote(eq(CASE_ID), eq(BUSINESS_KEY), eq("noteText"));
        verify(processScenario).hasCompleted("EndEvent");
    }

    @Test
    public void whenOfficialToMinisterialAndDirectionIsNotForward_thenItCompletesImmediatelyWithoutMakingAnyChanges() {

        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToMinisterial"))
            .thenReturn(task -> task.complete(withVariables(
                "valid", true,
                "DIRECTION", "BACKWARD")));

        Scenario.run(processScenario)
            .startByKey("MPAM_CHANGE_REFERENCE_TYPE", Map.of("RefType", "Official"))
            .execute();

        verify(bpmnService, never()).updateValue(any(), any(), any(), any(), any(), any());
        verify(bpmnService, never()).blankCaseValues(any(), any(), any(), any());
        verify(bpmnService, never()).updateCount(any(), anyString(), anyInt());
        verify(bpmnService, never()).createCaseConversionNote(anyString(), anyString(), anyString());
        verify(processScenario).hasCompleted("EndEvent");
    }

    @Test
    public void whenOfficialToMinisterialAndDirectionIsForwardButInvalid_thenFormIsRepresented() {

        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToMinisterial"))
            .thenReturn(task -> task.complete(withVariables(
                "valid", false,
                "DIRECTION", "FORWARD")))
            .thenReturn(task -> task.complete(withVariables(
                "valid", true,
                "DIRECTION", "FORWARD",
                "RefType", "Ministerial",
                "CaseNote_TriageChangeCaseType", "noteText",
                "RefTypeCorrection", "XXXX"
            )));

        Scenario.run(processScenario).startBy(() ->
            rule.getRuntimeService().startProcessInstanceByKey(
                "MPAM_CHANGE_REFERENCE_TYPE",
                BUSINESS_KEY,
                Map.of("CaseUUID", "CASE_ID", "RefType", "Official"))
        ).execute();

        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Ministerial"), eq("RefTypeStatus"), eq("Confirm"));
        verify(bpmnService, never()).blankCaseValues(any(), any(), any(), any());
        verify(bpmnService).updateCount(any(),eq("RefTypeUpdateCount"),eq(1));
        verify(bpmnService).createCaseConversionNote(eq(CASE_ID), eq(BUSINESS_KEY), eq("noteText"));
        verify(processScenario).hasCompleted("EndEvent");
        verify(processScenario, times(2)).hasFinished("Validate_ReferenceTypeToMinisterial");
    }
}
