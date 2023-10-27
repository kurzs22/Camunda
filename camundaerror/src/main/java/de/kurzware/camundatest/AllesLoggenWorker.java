package de.kurzware.camundatest;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class AllesLoggenWorker {

    private final int SLEEP_MILLIS = 3_000;
    private final int ARRAY_SIZE = 500;
    private final int LOOP_SIZE = 400_000;

    @JobWorker(type = "allesLoggen")
    public Map<String, Object> allesLoggen(final ActivatedJob job) throws InterruptedException {
        log.info("allesLoggen: {}", job);
        Thread.sleep(SLEEP_MILLIS);
        //busyWait();
        return null;
    }

    private double busyWait() {
        double[] work = new double[ARRAY_SIZE];
        for (int i=0; i<ARRAY_SIZE; i++) {
            work[i] = 0.0;
        }
        for (int wait=0; wait<LOOP_SIZE; wait++) {
            for (int i=0; i<ARRAY_SIZE; i++) {
                work[i] += Math.sin(Math.sqrt(12345678.9 * wait));
            }
        }
        double sum = 0.0;
        for (int i=0; i<ARRAY_SIZE; i++) {
            sum += work[i];
        }
        return sum;
    }
}
