package com.example.client;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.PrincipalNameKeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Log4j2
@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes(DiscoveryClient dc) {
		return route()
			.GET("/instances", s -> ServerResponse.ok().syncBody(dc.getInstances("greetings-service")))
			.build();
	}

	@Bean
	RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(5, 7);
	}

	@Bean
	MapReactiveUserDetailsService authentication() {
		return new MapReactiveUserDetailsService(User
			.withDefaultPasswordEncoder().username("jlong").password("pw").roles("USER").build());
	}


	@Bean
	SecurityWebFilterChain authorization(ServerHttpSecurity httpSecurity) {
		httpSecurity.httpBasic();
		httpSecurity.csrf().disable();
		httpSecurity
			.authorizeExchange()
			.pathMatchers("/hi").authenticated()
			.anyExchange().permitAll();
		return httpSecurity.build();
	}

	@Bean
	RouteLocator gateway(RouteLocatorBuilder rlb) {
		return rlb
			.routes()
			.route(rs -> rs
				.path("/hi/*")
				.filters(fs -> fs
					.rewritePath("/hi/(?<name>.*)", "/greetings/${name}")
					.requestRateLimiter(rlc -> rlc
						.setRateLimiter(redisRateLimiter())
						.setKeyResolver(new PrincipalNameKeyResolver()) // default
					)
				)
				.uri("lb://greetings-service")
			)
			.build();
	}
}

