package org.fkjava.security.sms;

import org.fkjava.identity.IdentityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@ComponentScan("org.fkjava")
@EnableJpaRepositories() // 激活JPA的自动DAO扫描
@EnableRedisRepositories("org.fkjava")
public class SMSConfig {


    public static void main(String[] args) {
        SpringApplication.run(SMSConfig.class, args);
    }
}
