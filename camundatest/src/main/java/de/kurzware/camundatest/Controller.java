package de.kurzware.camundatest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class Controller {

    public final String VAR_SCORING_RESULT = "scoringResult";
    public final static String ZEEBE_MESSAGE_SCORING = "Message_Nachricht";

    public final ZeebeClient zeebeClient;

    @PostMapping
    public ResponseEntity<String> post(@RequestBody String body) {
        ProcessInstanceEvent processTest = zeebeClient.newCreateInstanceCommand().bpmnProcessId("Process_Test")
                .latestVersion()
                .send()
                .join();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping("/message")
    public ResponseEntity<String> messageReceived(@RequestBody String responseString) {
        log.info("Received HTTP " + responseString);

        Map<String, Object> variables = new HashMap<>();
        variables.put(VAR_SCORING_RESULT, 42l);

        // Route message to workflow
        zeebeClient.newPublishMessageCommand() //
                .messageName(ZEEBE_MESSAGE_SCORING) //
                .correlationKey(responseString) //
                .variables(variables) //
                .send().join();

        return new ResponseEntity<>(responseString, HttpStatus.OK);
    }

}
