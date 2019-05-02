package com.example.reactiveadapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@RestController
@SpringBootApplication
@Log4j2
@RequiredArgsConstructor
public class ReactiveAdapterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveAdapterApplication.class, args);
	}

	private final ApplicationEventPublisher publisher;
	private final Bridger bridger;

	@PostMapping("/greet/{name}")
	Mono<Void> postGreeting(@PathVariable String name) {
		this.publisher.publishEvent(new GreetingsEvent("hello " + name + " @ " + Instant.now()));
		return Mono.empty();
	}

	@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE, value = "/sse/greetings")
	Publisher<GreetingsEvent> events() {
		return Flux.create(this.bridger);
	}


}

@Component
class Bridger implements ApplicationListener<GreetingsEvent>, Consumer<FluxSink<GreetingsEvent>> {


	private final LinkedBlockingQueue<GreetingsEvent> queue = new LinkedBlockingQueue<>();

	@Override
	public void onApplicationEvent(GreetingsEvent greetingsEvent) {
		this.queue.offer(greetingsEvent);
	}

	@Override
	public void accept(FluxSink<GreetingsEvent> sink) {

		try {
			GreetingsEvent event;
			while ((event = this.queue.take()) != null) {
				sink.next(event);
			}
		}
		catch (InterruptedException e) {
			ReflectionUtils.rethrowRuntimeException(e);
		}
	}

}


class GreetingsEvent extends ApplicationEvent {

	public GreetingsEvent(String greeting) {
		super(greeting);
	}
}