package de.kurzware.camundawait;

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
import java.util.UUID;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class Controller {

    public final String VAR_SCORING_REQUEST_ID = "requestId";
    public final String VAR_SCORING_RESULT = "scoringResult";

    public final ZeebeClient zeebeClient;
    private final RestTemplate restTemplate;

    @PostMapping("/wait")
    public ResponseEntity<String> messageReceived(@RequestBody String responseString) {
        log.info("Wait Received HTTP " + responseString);

        if (responseString==null || responseString.isEmpty()) {
            responseString = UUID.randomUUID().toString();
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put(VAR_SCORING_REQUEST_ID, responseString);
        variables.put(VAR_SCORING_RESULT, 42L);

        ProcessInstanceEvent processTest = zeebeClient.newCreateInstanceCommand().bpmnProcessId("Process_Warten")
                .latestVersion()
                .variables(variables) //
                .send()
                .join();

        return new ResponseEntity<>(responseString, HttpStatus.OK);
    }

}
