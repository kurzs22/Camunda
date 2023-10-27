package de.kurzware.camundatest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class Controller {

    public final String VAR_SCORING_RESULT = "scoringResult";
    public final static String ZEEBE_MESSAGE_SCORING = "Message_Nachricht";
    public final static int SLEEP_MILLIS = 10_000;

    public final ZeebeClient zeebeClient;
    private final RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<String> post(@RequestBody String body) {
        ProcessInstanceEvent processTest = zeebeClient.newCreateInstanceCommand().bpmnProcessId("Process_TestError")
                .latestVersion()
                .send()
                .join();
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @PostMapping("/message")
    public ResponseEntity<String> messageReceived(@RequestBody String responseString) {
        log.info("Received HTTP " + responseString);

        Map<String, Object> variables = new HashMap<>();
        variables.put(VAR_SCORING_RESULT, 42L);

        // Route message to workflow
        zeebeClient.newPublishMessageCommand() //
                .messageName(ZEEBE_MESSAGE_SCORING) //
                .correlationKey(responseString) //
                .variables(variables) //
                .send().join();

        return new ResponseEntity<>(responseString, HttpStatus.OK);
    }

    @PostMapping("/wait")
    public ResponseEntity<String> waitReceived(@RequestBody String responseString) throws InterruptedException {
        log.info("Wait Received HTTP " + responseString);

        Thread.sleep(SLEEP_MILLIS);

        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8080/message",
                responseString, String.class);

        return new ResponseEntity<>(responseString, HttpStatus.OK);
    }

}
