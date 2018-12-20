package uk.gov.digital.ho.hocs.workflow.domain.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.UNCAUGHT_EXCEPTION;


@ControllerAdvice
@Slf4j
public class ResourceExceptionHandler {

    @ExceptionHandler(EntityCreationException.class)
    public ResponseEntity handle(EntityCreationException e) {
        log.error("EntityCreationException: {}", e.getMessage(), value(EVENT, e.getEvent()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handle(EntityNotFoundException e) {
        log.error("EntityNotFoundException: {}", e.getMessage(), value(EVENT, e.getEvent()));
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handle(Exception e) {
        log.error("Exception: {}", e.getMessage(), value(EVENT,UNCAUGHT_EXCEPTION));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }


}
