package com.niewj.springboot;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class Springboot03LoggingApplicationTests {

    Logger log = LoggerFactory.getLogger(getClass());
    @Test
    public void log() {
        log.trace("trace 级别");
        log.debug("debug 级别");
        log.info("info 级别");
        log.warn("warn 级别");
        log.error("error 级别");
    }

}
