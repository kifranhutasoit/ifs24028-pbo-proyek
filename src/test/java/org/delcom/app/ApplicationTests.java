package org.delcom.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class ApplicationTest {

	@Test
	void mainMethod_ShouldRunSpringApplication() throws Exception {
		// Mock SpringApplication.run untuk test main method
		try (var mockedSpring = mockStatic(SpringApplication.class)) {
			ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
			mockedSpring.when(() -> SpringApplication.run(Application.class, new String[] {}))
					.thenReturn(mockContext);

			// Jalankan main method
			assertDoesNotThrow(() -> Application.main(new String[] {}));

			// Verify SpringApplication.run dipanggil
			mockedSpring.verify(() -> SpringApplication.run(Application.class, new String[] {}));
		}
	}

	@Test
	void contextLoads_ShouldNotThrowException() throws Exception {
		// Test bahwa Spring context bisa dimuat
		assertDoesNotThrow(() -> {
			// Test basic class loading
			Class<?> clazz = Class.forName("org.delcom.app.Application");
			assertNotNull(clazz);
		});
	}

	@Test
	void todoApplication_ShouldHaveSpringBootAnnotation() throws Exception {
		// Test bahwa class memiliki annotation @SpringBootApplication
		assertNotNull(Application.class
				.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class));
	}

	@Test
	void todoApplication_CanBeInstantiated() throws Exception {
		// Test bahwa kita bisa membuat instance Application
		assertDoesNotThrow(() -> {
			Application app = new Application();
			assertNotNull(app);
		});
	}
}
