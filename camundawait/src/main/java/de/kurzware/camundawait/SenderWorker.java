package de.kurzware.camundawait;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SenderWorker {

    public final String VAR_SCORING_REQUEST_ID = "requestId";
    public final String VAR_SCORING_RETRY_COUNT = "scoringRetryCount";

    private final RestTemplate restTemplate;

    @JobWorker(type = "nachrichtWeiterleiten")
    public Map<String, Object> sendMessage(ActivatedJob job) {
        log.info("Forward HTTP message to score customer [" + job + "]");

        Map<String, Object> existingVariables = job.getVariablesAsMap();
        Map<String, Object> newVariables = new HashMap<>();

        // reuse request id or initialize (you could also generate a fresh id for every request/response cycle if you don't want to correlate responses from earlier retries, I don't care, I am also happy about "old" responses in this example)
        String scoringRequestId = (String) existingVariables.get(VAR_SCORING_REQUEST_ID);
        if (scoringRequestId==null) {
            scoringRequestId = UUID.randomUUID().toString();
            newVariables.put(VAR_SCORING_REQUEST_ID, scoringRequestId);
        }

        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8080/message",
                scoringRequestId, String.class);

        // decrease retry counter
        int counter = (Integer) existingVariables.getOrDefault(VAR_SCORING_RETRY_COUNT, 0);
        counter++;
        newVariables.put(VAR_SCORING_RETRY_COUNT, counter);

        return newVariables;
    }
}
