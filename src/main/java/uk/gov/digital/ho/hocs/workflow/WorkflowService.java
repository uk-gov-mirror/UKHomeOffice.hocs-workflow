package uk.gov.digital.ho.hocs.workflow;

import org.apache.camel.spi.RestRegistry;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.workflow.dto.*;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.workflow.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;
import uk.gov.digital.ho.hocs.workflow.model.CaseTypeDetails;
import uk.gov.digital.ho.hocs.workflow.model.DocumentData;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.util.*;

@Service
public class WorkflowService {

    private static String CASE_SERVICE;
    private static RestTemplate restTemplate;

    private static List<CaseTypeDetails> caseTypeDetails = new ArrayList<>();

    static {
        caseTypeDetails.add(new CaseTypeDetails("DCU", "DCU MIN", CaseType.MIN));
        caseTypeDetails.add(new CaseTypeDetails("DCU", "DCU TRO", CaseType.TRO));
        caseTypeDetails.add(new CaseTypeDetails("DCU", "DCU DTEN", CaseType.DTEN));
        caseTypeDetails.add(new CaseTypeDetails("UKVI", "UKVI BREF", CaseType.BREF));
    }

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    @Autowired
    public WorkflowService(RuntimeService runtimeService, TaskService taskService, RestTemplate restTemp, @Value("${hocs.case-service}") String caseService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        restTemplate = restTemp;
        CASE_SERVICE = caseService;
    }

    public List<CaseTypeDetails> getAllWorkflowTypes() {
        if(caseTypeDetails != null && !caseTypeDetails.isEmpty()){
            return caseTypeDetails;
        } else {
            return new ArrayList<>();
        }
    }

    public CreateWorkflowCaseResponse createNewCase(CaseType caseType) throws EntityCreationException, EntityNotFoundException {

        // Validate the CaseType
        if (caseType != null) {

            // Create Empty Case
            CreateCaseResponse caseResponse = createCase(caseType);

            // Instantiate the Case level workflow
            // Get the first stage name from case level workflow
            StageType stageType = startCaseProcess(caseType, caseResponse.getUuid());

            // Set current Stage, User,Team,Unit ...?
            //updateCase(stageResponse.getUuid(), stageType, username);

            // Return
            return new CreateWorkflowCaseResponse(caseResponse.getCaseReference(), caseResponse.getUuid());

        } else {
            throw new EntityCreationException("Failed to create case, invalid caseType!");
        }
    }

    public StartStageResponse startStage(UUID caseUUID, UUID stageUUID) throws EntityNotFoundException {

        StageType stageType = StageType.DCU_MIN_CATEGORISE;

        // Create Empty Stage
        CreateStageResponse stageResponse = createStage(stageType, caseUUID);

        // Instantiate the Stage level workflow
        // Get the first screen name
        String screenName = startStageProcess(stageType, stageResponse.getUuid());

        return new StartStageResponse(stageType, screenName);

    }

    public String updateCase(UUID caseUUID, UUID stageUUID) throws EntityNotFoundException {

        Task task = taskService.createTaskQuery().processInstanceBusinessKey(stageUUID.toString()).list().get(0);

        Map<String, Object> values = new HashMap<>();
        values.put("valid", true);
        values.put("decision", "OGD");
        taskService.complete(task.getId(), values);

        return getValueFromBusinessKey(stageUUID, "screen");
    }

    // STUB
    private CreateCaseResponse createCase(CaseType caseType) {

        // Post to /case
        CreateCaseRequest createCaseRequest = new CreateCaseRequest(caseType);
        ResponseEntity<CreateCaseResponse> createCaseResponse = restTemplate.postForEntity(CASE_SERVICE + "/case", createCaseRequest, CreateCaseResponse.class);

        return createCaseResponse.getBody();
    }

    // STUB
    private void updateCase(UUID caseUUID, StageType stageType, String username) {
        // Post to /case/{caseUUID}
        // UpdateCaseRequest updateCaseRequest = new UpdateCaseRequest();
        // Do nothing.
    }

    // STUB
    private CreateStageResponse createStage(StageType stageType, UUID caseUUID) {
        // Post to /case/{caseUUID}/stage
        CreateStageRequest createStageRequest = new CreateStageRequest(stageType);
        return new CreateStageResponse(UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d"));
    }

    private StageType startCaseProcess(CaseType caseType, UUID caseUUID) throws EntityNotFoundException {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(caseType.toString(), caseUUID.toString(), new HashMap<>());
        String stageType = getValueFromProcess(processInstance.getProcessInstanceId(), "stage");
        return StageType.valueOf(stageType);
    }

    private String startStageProcess(StageType stageName, UUID stageUUID) throws EntityNotFoundException {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(stageName.name(), stageUUID.toString(), new HashMap<>());
        return getValueFromProcess(processInstance.getProcessInstanceId(), "screen");
    }

    private String getValueFromBusinessKey(UUID businessKey, String value) throws EntityNotFoundException {
        if (businessKey != null) {
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceBusinessKey(businessKey.toString())
                    .singleResult();

            if (instance == null) {
                throw new EntityNotFoundException("ProcessInstance not found, businessKey: '" + businessKey + "'");
            }

            return getValueFromProcess(instance.getProcessInstanceId(), value);

        } else {
            throw new EntityNotFoundException("ProcessInstance not found, businessKey is null!");
        }
    }

    private String getValueFromProcess(String processInstanceId, String value) throws EntityNotFoundException {
        if (processInstanceId != null) {
            VariableInstance variableInstance = runtimeService.createVariableInstanceQuery()
                    .processInstanceIdIn(processInstanceId)
                    .variableName(value).singleResult();

            if (variableInstance == null) {
                throw new EntityNotFoundException("VariableInstance not found, processInstanceId: '" + processInstanceId + " ','" + value + "'");
            }

            return (String) variableInstance.getValue();

        } else {
            throw new EntityNotFoundException("ProcessInstance not found, processInstanceId is null!");
        }
    }


    public void addDocument(List<DocumentData> documentDataList) throws EntityCreationException {

        // for each
        for(DocumentData documentData : documentDataList) {

            UUID docUUID = UUID.randomUUID();

            //restTemplate.postForEntity("", documentData, AddDocumentResponse);
            // post to /case/{caseUUID}/document { docUUID, displayName, documentType }

            // post to queue { caseUUID, docUUID s3UntrustedUrl}
        }

    }

}
