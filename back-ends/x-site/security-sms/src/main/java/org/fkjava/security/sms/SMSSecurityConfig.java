package org.fkjava.security.sms;

import org.fkjava.security.interceptors.UserHolderInterceptor;
import org.fkjava.security.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableJpaRepositories
@ComponentScan("org.fkjava")
public class SMSSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {


    private final SecurityService securityService;
    private final PasswordEncoder passwordEncoder;
    private static final String REMEMBER_KEY = "fkjava.secure.keys";


    @Autowired
    public SMSSecurityConfig(SecurityService securityService, PasswordEncoder passwordEncoder) {
        this.securityService = securityService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserHolderInterceptor())//
                .addPathPatterns("/**")//
        // 默认Spring Security的拦截器，已经在其他的拦截器之前
        // 所以不使用order也是可以有效的
        // 如果不能正常获取到User（通过UserHolder），那么就需要修改顺序。
        // .order(Integer.MAX_VALUE)// 排在最后
        ;
    }

    // 配置基于HTTP的安全控制
    @Override
    protected void configure(HttpSecurity http) throws Exception {

    }

    @Bean
    public RememberMeServices rememberMeServices() {
        return new TokenBasedRememberMeServices(REMEMBER_KEY, securityService);
    }


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/sms/index").setViewName("security/sms/index");
        registry.addViewController("/security/sms/login").setViewName("security/sms/login");
        // 欢迎页，访问根目录重定向到一个首页
        // registry.addViewController("/").setViewName("security/index");
        registry.addRedirectViewController("/", "/sms/index");
    }

    public static void main(String[] args) {
        SpringApplication.run(SMSSecurityConfig.class, args);
    }
}
