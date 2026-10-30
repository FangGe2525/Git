package com.community.repair;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.community.repair.mapper")
@EnableScheduling
public class CommunityRepairApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityRepairApplication.class, args);
    }
}
