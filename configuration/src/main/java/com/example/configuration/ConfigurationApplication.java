package com.example.configuration;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

@Log4j2
@EnableConfigurationProperties(BootifulProperties.class)
@SpringBootApplication
public class ConfigurationApplication {

    public static void main(String[] args) {
        // System.setProperty("spring.config.name", "foo");
        // System.setProperty("spring.profiles.active", "dev");
        SpringApplication.run(ConfigurationApplication.class, args);

//        new SpringApplicationBuilder()
//                .sources(ConfigurationApplication.class)
//                .initializers(context -> context.getEnvironment().getPropertySources().addFirst(new BootifulPropertySource()))
//                .run(args);

    }

    ConfigurationApplication(Environment environment,
                             @Value("${message:${msg:Hello}}") String msg,
                             BootifulProperties properties) {
        var message = environment.getProperty("message");
        log.info("the environment says " + message);
        var favoriteNumber = environment.getProperty("favorite-number", Integer.class);
        log.info("the environment says " + favoriteNumber);
        log.info("the @Value annotation is " + msg);
        var configPropsMessage = properties.getMessage();
        log.info("configuration properties message: " + configPropsMessage);
    }

}

@Log4j2
@Component
@RefreshScope
@RequiredArgsConstructor
class ConfigClient {

    private final Environment environment;

    @EventListener
    public void refresh(RefreshScopeRefreshedEvent event) {
        log.info(this.environment.getProperty("message-from-config-server"));
        log.info(event.getName());
        log.info(event.getSource());
    }
}


@Log4j2
@Component
class VaultClient {

    VaultClient(@Value("${message-from-vault-server}") String vaultValue) {
        log.info("vault value:" + vaultValue);
    }
}

@Data
@ConfigurationProperties("bootiful")
class BootifulProperties {
    private String message;
}

class BootifulEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        environment.getPropertySources().addFirst(new BootifulPropertySource());
    }
}


class BootifulPropertySource extends PropertySource<String> {

    public BootifulPropertySource() {
        super("bootiful");
    }

    @Override
    public Object getProperty(String name) {
        if (name.equalsIgnoreCase("message"))
            return "Hello, " + this.getClass().getSimpleName() + "!";
        return null;
    }
}