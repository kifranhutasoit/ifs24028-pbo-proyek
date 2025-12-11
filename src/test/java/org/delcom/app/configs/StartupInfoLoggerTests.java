package org.delcom.app.configs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class StartupInfoLoggerTests {

    private StartupInfoLogger logger;

    private ConfigurableEnvironment environment;
    private ConfigurableApplicationContext context;
    private ApplicationReadyEvent event;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setup() {
        // Redirect System.out
        System.setOut(new PrintStream(outContent));

        logger = new StartupInfoLogger();

        environment = mock(ConfigurableEnvironment.class);
        context = mock(ConfigurableApplicationContext.class);
        event = mock(ApplicationReadyEvent.class);

        when(event.getApplicationContext()).thenReturn(context);
        when(context.getEnvironment()).thenReturn(environment);

        // default property mock
        when(environment.getProperty("server.port", "8080")).thenReturn("8080");
        when(environment.getProperty("spring.devtools.livereload.enabled", Boolean.class, false)).thenReturn(true);
        when(environment.getProperty("spring.devtools.livereload.port", "35729")).thenReturn("35729");
        when(environment.getProperty("server.address", "localhost")).thenReturn("localhost");
    }

    @Test
    void testOnApplicationEvent_printsExpectedOutput() {
        when(environment.getProperty("server.servlet.context-path", "")).thenReturn("/app/home");
        logger.onApplicationEvent(event);

        String output = outContent.toString();

        assertTrue(output.contains("Application started successfully!"));
        assertTrue(output.contains("> URL: http://localhost:8080"));
        assertTrue(output.contains("> LiveReload: ENABLED (port 35729)"));
    }

    @Test
    void testLiveReloadDisabled() {
        when(environment.getProperty("server.servlet.context-path", "/")).thenReturn("");
        when(environment.getProperty("spring.devtools.livereload.enabled", Boolean.class, false)).thenReturn(false);

        logger.onApplicationEvent(event);

        String output = outContent.toString();

        assertTrue(output.contains("> LiveReload: DISABLED"));
    }
}
