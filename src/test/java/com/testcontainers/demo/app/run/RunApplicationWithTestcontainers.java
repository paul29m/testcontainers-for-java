package com.testcontainers.demo.app.run;

import com.testcontainers.demo.DemoApplication;
import com.testcontainers.demo.app.config.KafkaAppRunContainerConfig;
import com.testcontainers.demo.app.config.MockGitClientComponent;


import org.springframework.boot.SpringApplication;

import java.io.IOException;

public class RunApplicationWithTestcontainers {

    public static void main(String[] args) throws IOException {
        MockGitClientComponent.setupGitClientMock();
        args = new String[]{"--spring.profiles.active=local-container"};
        SpringApplication.from(DemoApplication::main).with(KafkaAppRunContainerConfig.class).run(args);
    }

}
