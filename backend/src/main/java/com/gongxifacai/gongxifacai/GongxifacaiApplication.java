package com.gongxifacai.gongxifacai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GongxifacaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GongxifacaiApplication.class, args);
    }

}
