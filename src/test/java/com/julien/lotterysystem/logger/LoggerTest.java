package com.julien.lotterysystem.logger;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class LoggerTest {

    @Test
    void logInfo() {
        System.out.println("hi");
        log.info("hey!");
    }
}
