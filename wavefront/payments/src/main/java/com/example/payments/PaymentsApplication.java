package com.example.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.util.Collections;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@EnableBinding(Source.class)
@SpringBootApplication
@RequiredArgsConstructor
public class PaymentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentsApplication.class, args);
    }

    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;


    @SneakyThrows
    private String json(Object o) {
        return this.objectMapper.writeValueAsString(o);
    }

    @Bean
    RouterFunction<ServerResponse> httpEndpoints(WebClient http,
                                                 Source customerSatisfactionService) {

        Counter register = Counter
                .builder("payments.received")
                .tag("region", "us-west")
                .register(this.meterRegistry);

        return route()
                .GET("/payments", serverRequest -> {


                    register.increment(Math.random() * 100);

                    String orderId = serverRequest.queryParam("orderId").get();

                    Flux<String> httpRequest = http.get().uri("http://localhost:8090/fulfillment/{orderId}", orderId)
                            .retrieve()
                            .bodyToFlux(String.class);

                    String payload = json(Collections.singletonMap("orderId", orderId));
                    customerSatisfactionService.output().send(MessageBuilder.withPayload(payload).build());


                    if (Math.random() >= .9) {
                        return ServerResponse.badRequest().build();
                    } else {
                        return ServerResponse.ok().body(httpRequest, String.class);
                    }

                })
                .build();
    }

    @Bean
    WebClient httpCllient(WebClient.Builder builder) {
        return builder.build();
    }

}
