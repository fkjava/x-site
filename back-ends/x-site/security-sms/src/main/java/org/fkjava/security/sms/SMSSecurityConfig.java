package org.fkjava.security.sms;

import org.fkjava.security.interceptors.UserHolderInterceptor;
import org.fkjava.security.service.SecurityService;
import org.fkjava.sms.service.ShortMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SpringBootApplication
@EnableJpaRepositories
@ComponentScan("org.fkjava")
public class SMSSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    private final SecurityService securityService;
    private final ShortMessageService shortMessageService;
    private static final String REMEMBER_KEY = "fkjava.secure.keys";


    @Autowired
    public SMSSecurityConfig(@Qualifier("smsSecurityService") SecurityService securityService, ShortMessageService shortMessageService) {
        this.securityService = securityService;
        this.shortMessageService = shortMessageService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {

        SMSAuthenticationProvider provider = new SMSAuthenticationProvider();
        provider.setUserDetailsService(securityService);
        provider.setShortMessageService(shortMessageService);
        auth.authenticationProvider(provider);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserHolderInterceptor())//
                .addPathPatterns("/**")//
        ;
    }

    // 配置基于HTTP的安全控制
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String loginPage = "/security/sms/login";
        String doLoginPage = "/security/sms/do-login";
        String logoutPage = "/security/sms/do-logout";
        String defaultIndex = "/security/sms/index";

        // 在UsernamePasswordAuthenticationFilter之前添加一个短信验证的过滤器，
        // 用于把用户输入的短信验证码转换为SMSAuthenticationToken对象。
        SMSAuthenticationFilter filter = new SMSAuthenticationFilter(doLoginPage);
        AuthenticationManager am = super.authenticationManager();
        filter.setAuthenticationManager(am);
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler(loginPage + "?error"));
        filter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler(defaultIndex));

//        TokenBasedRememberMeServices rememberMeServices = rememberMeServices();
//        rememberMeServices.setUseSecureCookie(false);
//        filter.setRememberMeServices(rememberMeServices);

        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);


        http.authorizeRequests()// 验证请求
                // 登录页面的地址和其他的静态页面都不要权限
                .antMatchers(loginPage, "/favicon.ico",
                        "/security/register", "/sms/verify", "/webjars/**",
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        logoutPage)//
                .permitAll()// 不做访问判断
                .antMatchers(defaultIndex).authenticated()// 授权以后才能访问，但不使用自定义检查
                .anyRequest().authenticated()
                .and().formLogin().loginPage(loginPage)
                .and().logout()// 配置退出登录
                .logoutUrl(logoutPage)
                .logoutSuccessUrl(loginPage)
//                .and().csrf()// 激活防跨站攻击功能
//                .and().cors()//激活跨域资源共享
                .and().rememberMe()// 记住登录状态
                .useSecureCookie(false)//
                .userDetailsService(securityService)//
                .key(REMEMBER_KEY)//
        ;
    }

    @Bean
    public TokenBasedRememberMeServices rememberMeServices() {
        return new TokenBasedRememberMeServices(REMEMBER_KEY, securityService);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/security/sms/index").setViewName("security/sms/index");
        registry.addViewController("/security/sms/login").setViewName("security/sms/login");
        // 欢迎页，访问根目录重定向到一个首页
        // registry.addViewController("/").setViewName("security/index");
        registry.addRedirectViewController("/", "/index");
    }

    public static void main(String[] args) {
        SpringApplication.run(SMSSecurityConfig.class, args);
    }
}
