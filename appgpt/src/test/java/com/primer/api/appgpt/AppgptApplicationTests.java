package com.primer.api.appgpt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifica que el contexto de la aplicación (beans, seguridad, JWT,
 * WebSocket, etc.) se carga correctamente.
 *
 * Se fija el dialecto de Hibernate explícitamente para que la prueba
 * no requiera una conexión real a MySQL al iniciar.
 */
@SpringBootTest(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
})
class AppgptApplicationTests {

	@Test
	void contextLoads() {
	}

}
