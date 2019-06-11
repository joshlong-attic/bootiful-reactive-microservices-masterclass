package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.tools.agent.ReactorDebugAgent;
import reactor.util.context.Context;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Log4j2
@RequiredArgsConstructor
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		BlockHound.install();
//		Hooks.onOperatorDebug();

		ReactorDebugAgent.init();
		ReactorDebugAgent.processExistingClasses();

		SpringApplication.run(DemoApplication.class, args);
	}

	//	@EventListener(ApplicationReadyEvent.class)
	public void streams() throws Exception {

		var letters = Flux
			.just("A", "B", "C", "D")
			.flatMap(letter -> {
				log.info("letter: " + letter + " on " + Thread.currentThread().getName());
				//Assert.isTrue(!letter.equalsIgnoreCase("b"), "No B's allowed!");
				return Mono.just(letter.toLowerCase());
			});
		var numbers = Flux.just(1, 2, 3, 4).map(i -> {
			log.info("number: " + i);
			return i * i;
		});

		letters
			.thenMany(numbers)
//			.subscribeOn(Schedulers.newParallel("p1"))
			.subscribeOn(Schedulers.elastic())
			.log()
			.doOnError(Exception.class, log::error)
			.subscribe();

		//
		Flux
			.zip(letters, numbers)
			.subscribe(tuple -> log.info(tuple.getT1() + ":" + tuple.getT2()));
	}

	//@EventListener(ApplicationReadyEvent.class)
	public void coldAndHot() throws Exception {
		Flux<String> cold = Flux
			.fromStream(Stream.generate(() -> "Hello @ " + Instant.now() + "!"))
			.delayElements(Duration.ofSeconds(2));
		var hot = cold.share();
		hot.subscribe(log::info);
		Thread.sleep(1_000);
		hot.subscribe(log::info);
	}

	//	@EventListener(ApplicationReadyEvent.class)
	public void context() {
		Flux.just("A", "B", "C", "D")
			.map(String::toLowerCase)
			.doOnEach(signal -> {
				log.info("current signal: " + signal.getType());
				log.info("current value: " + signal.get());
				log.info("current ID value: " + signal.getContext().getOrDefault("id", "NOPE!"));
			})
			.subscriberContext(Context.of("id", UUID.randomUUID().toString()))
			.subscribe(log::info);
	}


	//	@EventListener(ApplicationReadyEvent.class)
	public void funWithSinks() throws Exception {

		AtomicReference<FluxSink<PingEvent>> sink = new AtomicReference<>();

		Flux<PingEvent> pef = Flux.create(sink::set);
		pef.subscribe(log::info);

		Thread.sleep(1_000);
		sink.get().next(new PingEvent());

		Thread.sleep(1_000);
		Thread.sleep(1_000);
		sink.get().next(new PingEvent());

	}


	private final PingEventAdapter adapter;

	@EventListener(ApplicationReadyEvent.class)
	public void reactorAdapter() throws Exception {

		Flux.create(this.adapter)
			.subscribeOn(Schedulers.elastic())
			.subscribe(log::info);
	}

}

@Component
@Log4j2
class PingEventAdapter implements Consumer<FluxSink<PingEvent>> {

	private final AtomicReference<FluxSink<PingEvent>> sink = new AtomicReference<>();

	@EventListener
	public void onNewPingEvent(PingEvent pingEvent) {
		FluxSink<PingEvent> pingEventFluxSink = this.sink.get();
		if (pingEventFluxSink != null) {
			pingEventFluxSink.next(pingEvent);
		}
	}

	@Override
	public void accept(FluxSink<PingEvent> sink) {
		this.sink.set(sink);
	}
}

class PingEvent extends ApplicationEvent {

	public PingEvent() {
		super(Instant.now().toString());
	}
}


@RestController
@RequiredArgsConstructor
class PingRestController {

	private final ApplicationEventPublisher publisher;

	@PostMapping("/ping")
	ResponseEntity<?> ping() {
		this.publisher.publishEvent(new PingEvent());
		return ResponseEntity.ok().build();
	}
}
