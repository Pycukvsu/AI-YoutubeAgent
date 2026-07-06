package com.youtubeagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YoutubeAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(YoutubeAgentApplication.class, args);
    }
}
