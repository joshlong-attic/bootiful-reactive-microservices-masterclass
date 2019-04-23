package com.example.cf;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v3.applications.ListApplicationsRequest;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class CfApplication {

	public static void main(String[] args) {
		SpringApplication.run(CfApplication.class, args);
	}

	@Bean
	DefaultCloudFoundryOperations cloudFoundryOperations(CloudFoundryClient cloudFoundryClient,
																																																						DopplerClient dopplerClient,
																																																						UaaClient uaaClient,
																																																						@Value("${cf.org}") String organization,
																																																						@Value("${cf.space}") String space) {
		return DefaultCloudFoundryOperations
			.builder()
			.cloudFoundryClient(cloudFoundryClient)
			.dopplerClient(dopplerClient)
			.uaaClient(uaaClient)
			.organization(organization)
			.space(space)
			.build();
	}

	@Bean
	DefaultConnectionContext connectionContext(@Value("${cf.api}") String apiHost) {
		return DefaultConnectionContext
			.builder()
			.apiHost(apiHost.split("://")[1])
			.build();
	}

	@Bean
	PasswordGrantTokenProvider tokenProvider(
		@Value("${cf.user}") String username,
		@Value("${cf.password}") String password) {
		return PasswordGrantTokenProvider.builder()
			.password(password)
			.username(username)
			.build();
	}

	@Bean
	ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
		return ReactorCloudFoundryClient
			.builder()
			.connectionContext(connectionContext)
			.tokenProvider(tokenProvider)
			.build();
	}

	@Bean
	ReactorDopplerClient dopplerClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
		return ReactorDopplerClient
			.builder()
			.connectionContext(connectionContext)
			.tokenProvider(tokenProvider)
			.build();
	}

	@Bean
	ReactorUaaClient uaaClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
		return ReactorUaaClient
			.builder()
			.connectionContext(connectionContext)
			.tokenProvider(tokenProvider)
			.build();
	}
}

@Log4j2
@Component
@RequiredArgsConstructor
class Demo {

	private final CloudFoundryClient cloudFoundryClient;

	@EventListener(ApplicationReadyEvent.class)
	public void go() throws Exception {
		this.cloudFoundryClient
			.applicationsV3()
			.list(ListApplicationsRequest.builder().build())
			.flatMapMany(listApplicationsResponse -> Flux.fromIterable(listApplicationsResponse.getResources()))
			.subscribe(log::info);

	}
}