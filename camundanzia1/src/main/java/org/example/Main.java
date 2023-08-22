package org.example;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import org.example.handler.CreditCardServiceHandler;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

// Example from Tutorial
public class Main {

  private static final String ZEEBE_ADDRESS="048cf0f5-6990-49c3-aee7-c31a2db6d3b3.jfk-1.zeebe.camunda.io:443";
  private static final String ZEEBE_CLIENT_ID="A5UW2RUpyH-3_vta81S767_Sf6sxnl.I";
  private static final String ZEEBE_CLIENT_SECRET="syEjnlnGXmb_qDD0jwaPWAU4BQMLr_7itJSdoJKkx3vk18HLxV6gdfZmUS9TtZJD";
  private static final String ZEEBE_AUTHORIZATION_SERVER_URL="https://login.cloud.camunda.io/oauth/token";
  private static final String ZEEBE_TOKEN_AUDIENCE="zeebe.camunda.io";

  public static void main(String[] args) {

    final OAuthCredentialsProvider credentialsProvider =
        new OAuthCredentialsProviderBuilder()
            .authorizationServerUrl(ZEEBE_AUTHORIZATION_SERVER_URL)
            .audience(ZEEBE_TOKEN_AUDIENCE)
            .clientId(ZEEBE_CLIENT_ID)
            .clientSecret(ZEEBE_CLIENT_SECRET)
            .build();

    try (final ZeebeClient client =
        ZeebeClient.newClientBuilder()
            .gatewayAddress(ZEEBE_ADDRESS)
            .credentialsProvider(credentialsProvider)
//            .usePlaintext()
            .build()) {
      final Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("reference", "C8_12345");
      variables.put("amount", Double.valueOf(100.00));
      variables.put("cardNumber", "1234567812345678");
      variables.put("cardExpiry", "12/2023");
      variables.put("cardCVC", "123");

      client.newCreateInstanceCommand()
              .bpmnProcessId("paymentProcess")
              .latestVersion()
              .variables(variables)
              .send()
              .join();

      final JobWorker creditCardWorker =
              client.newWorker()
                      .jobType("chargeCreditCard")
                      .handler(new CreditCardServiceHandler())
                      .timeout(Duration.ofSeconds(10).toMillis())
                      .open();
      Thread.sleep(10000);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("Hello world!");
  }
}