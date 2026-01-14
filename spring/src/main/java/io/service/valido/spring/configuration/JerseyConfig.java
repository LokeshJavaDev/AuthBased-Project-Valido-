package io.service.valido.spring.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

@Configuration
@ApplicationPath("/valido")
public class JerseyConfig extends ResourceConfig {

    private final ApplicationContext applicationContext;

    public JerseyConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        register(JacksonFeature.class);
        register(io.service.valido.rest.auth.AuthResource.class);

    }

    @PostConstruct
    public void init() {
        if (applicationContext == null) return;

        Stream.concat(
                applicationContext.getBeansWithAnnotation(Path.class).values().stream(),
                applicationContext.getBeansWithAnnotation(Provider.class).values().stream()
        ).forEach(this::register);
    }
}