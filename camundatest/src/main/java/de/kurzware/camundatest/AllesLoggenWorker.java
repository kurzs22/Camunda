package de.kurzware.camundatest;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class AllesLoggenWorker {

    @JobWorker(type = "allesLoggen")
    public Map<String, Object> allesLoggen(final ActivatedJob job) throws InterruptedException {
        log.info("allesLoggen: {}", job);
        Thread.sleep(10000);
        return null;
    }
}
