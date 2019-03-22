package org.fkjava.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableJpaRepositories
public class CategoryConfig implements WebMvcConfigurer {

    public static void main(String[] args){
        SpringApplication.run(CategoryConfig.class, args);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
    }
}
