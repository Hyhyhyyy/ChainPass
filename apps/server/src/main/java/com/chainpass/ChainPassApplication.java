package com.chainpass;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ChainPass 跨境数字身份与合规支付原型启动类
 *
 * @author ChainPass Team
 */
@SpringBootApplication
@MapperScan({
    "com.chainpass.mapper",
    "com.chainpass.did.mapper",
    "com.chainpass.vc.mapper",
    "com.chainpass.payment.mapper",
    "com.chainpass.compliance.kyc"
})
@EnableScheduling
public class ChainPassApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChainPassApplication.class, args);
    }
}
