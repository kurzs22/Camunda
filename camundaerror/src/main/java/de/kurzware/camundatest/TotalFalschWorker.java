package de.kurzware.camundatest;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TotalFalschWorker {

    private final int SLEEP_MILLIS = 3_000;
    private final int ARRAY_SIZE = 500;
    private final int LOOP_SIZE = 400_000;

    public final ZeebeClient zeebeClient;

    @JobWorker(type = "totalFalsch", autoComplete = false)
    public Map<String, Object> totalFalsch(final ActivatedJob job) throws InterruptedException {
        log.info("throw Error: {}", job);

        zeebeClient.newThrowErrorCommand(job)
                .errorCode("technicalError")
                .errorMessage("Ohje, da ist was schief gelaufen :-(")
                .send()
                .exceptionally(t -> {throw new RuntimeException("Could not throw BPMN error: " + t.getMessage(), t);});

        return null;
    }

}
