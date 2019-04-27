package com.example.reactor.bridge;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Log4j2
@SpringBootApplication
public class ReactorApplication {

	private final EventProducerPublisherBridge bridge;

	public ReactorApplication(EventProducerPublisherBridge bridge) {
		this.bridge = bridge;
	}

	@Bean
	ScheduledExecutorService executor() {
		return Executors.newScheduledThreadPool(1);
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactorApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void go() {
		Flux.create(this.bridge).subscribe(log::info);
	}
}

@Component
class EventProducerPublisherBridge
	implements ApplicationListener<MyArbitraryEvent>, Consumer<FluxSink<MyArbitraryEvent>> {

	private final BlockingQueue<MyArbitraryEvent> queues = new LinkedBlockingDeque<>();

	@Override
	public void onApplicationEvent(MyArbitraryEvent event) {
		this.queues.offer(event);
	}

	@Override
	public void accept(FluxSink<MyArbitraryEvent> sink) {
		try {
			MyArbitraryEvent event;
			while ((event = this.queues.take()) != null) {
				sink.next(event);
			}
		}
		catch (Exception e) {
			ReflectionUtils.rethrowRuntimeException(e);
		}
	}
}

class MyArbitraryEvent extends ApplicationEvent {

	MyArbitraryEvent(Instant source) {
		super(source);
	}

	@Override
	public Instant getSource() {
		return Instant.class.cast(super.getSource());
	}
}

@Component
class EventProducer {

	private final ScheduledExecutorService executor;

	private final ApplicationEventPublisher publisher;

	EventProducer(ScheduledExecutorService executorService, ApplicationEventPublisher publisher) {
		this.executor = executorService;
		this.publisher = publisher;
	}

	@PostConstruct
	public void produce() {
		this.executor
			.scheduleAtFixedRate(() -> this.publisher.publishEvent(new MyArbitraryEvent(Instant.now())), 0, 1, TimeUnit.SECONDS);
	}
}

