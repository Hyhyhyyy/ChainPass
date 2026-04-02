package com.chainpass;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ChainPass 区块链身份验证系统启动类
 *
 * @author ChainPass Team
 */
@SpringBootApplication
@MapperScan("com.chainpass.mapper")
@EnableScheduling
public class ChainPassApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChainPassApplication.class, args);
    }
}