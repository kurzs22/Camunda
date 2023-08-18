package de.kurzware.camundatest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class MessageWorker {

    public final String VAR_SCORING_REQUEST_ID = "requestId";
    public final String VAR_SCORING_RETRY_COUNT = "scoringRetryCount";
    public final String VAR_SCORING_RESULT = "scoringResult";

    public final static String RABBIT_QUEUE_NAME_REQUEST = "scoringRequest";
    // using the request directly as response to avoid requiring another system :
    public final static String RABBIT_QUEUE_NAME_RESPONSE = "scoringRequest";
//    public final static String RABBIT_QUEUE_NAME_RESPONSE = "coringResponse";

    public final static String ZEEBE_TASK_TYPE_SEND_SCORING = "sendeNachricht";
    public final static String ZEEBE_MESSAGE_SCORING = "Message_Nachricht";

    @Autowired
    private ZeebeClient client;

    @Autowired
    protected RabbitTemplate rabbitTemplate;

    // Add Queues here to make sure they are auto-created by Spring
    @Bean
    Queue requestQueue() {
        return new Queue(RABBIT_QUEUE_NAME_REQUEST, false);
    }
    @Bean
    Queue responseQueue() {
        return new Queue(RABBIT_QUEUE_NAME_RESPONSE, false);
    }

    @JobWorker(type = ZEEBE_TASK_TYPE_SEND_SCORING)
    public Map<String, Object> sendScoringRequestMessage(final ActivatedJob job) {
        log.info("Send message to score customer [" + job + "]");

        Map<String, Object> existingVariables = job.getVariablesAsMap();
        Map<String, Object> newVariables = new HashMap<>();

        // reuse request id or initialize (you could also generate a fresh id for every request/response cycle if you don't want to correlate responses from earlier retries, I don't care, I am also happy about "old" responses in this example)
        String scoringRequestId = (String) existingVariables.get(VAR_SCORING_REQUEST_ID);
        if (scoringRequestId==null) {
            scoringRequestId = UUID.randomUUID().toString();
            newVariables.put(VAR_SCORING_REQUEST_ID, scoringRequestId);
        }

        // Send AMQP Message (using the default exchange created, see https://stackoverflow.com/questions/43408096/springamqp-rabbitmq-how-to-send-directly-to-queue-without-exchange)
        rabbitTemplate.convertAndSend(RABBIT_QUEUE_NAME_REQUEST, scoringRequestId);

        // decrease retry counter
        int counter = (Integer) existingVariables.getOrDefault(VAR_SCORING_RETRY_COUNT, 0);
        counter++;
        newVariables.put(VAR_SCORING_RETRY_COUNT, counter);

        return newVariables;
    }

    @RabbitListener(queues = RABBIT_QUEUE_NAME_RESPONSE)
    @Transactional
    public void messageReceived(String responseString) throws JsonMappingException, JsonProcessingException {
        log.info("Received " + responseString);

        Map<String, Object> variables = new HashMap<>();
        variables.put(VAR_SCORING_RESULT, 42l);

        // Route message to workflow
        client.newPublishMessageCommand() //
                .messageName(ZEEBE_MESSAGE_SCORING) //
                .correlationKey(responseString) //
                .variables(variables) //
                .send().join();
    }

}
