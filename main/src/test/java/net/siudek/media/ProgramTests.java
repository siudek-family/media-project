package net.siudek.media;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.shell.interactive.enabled=false"
})
class ProgramTests {

	@Test
	void contextLoads() {
	}

}
