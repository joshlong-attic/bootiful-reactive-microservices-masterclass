package com.example.fulfllments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class FulfllmentsApplication {


    @Bean
    RouterFunction<ServerResponse> http() {
        return route()
                .GET("/fulfillment/{fid}", r -> {
                    try {
                        Thread.sleep( (long)(Math.random() * 10000L));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("order-id: " + r.pathVariable("fid"));
                    return ServerResponse.ok().bodyValue(true);
                })
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(FulfllmentsApplication.class, args);
    }

}
