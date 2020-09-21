package com.ratel.config;

import com.ratel.framework.annotation.security.AnonymousAccess;
import com.ratel.modules.security.config.RatelAuthenticationProvider;
import com.ratel.modules.security.config.TokenConfigurer;
import com.ratel.modules.security.handler.JwtAccessDeniedHandler;
import com.ratel.modules.security.handler.JwtAuthenticationEntryPoint;
import com.ratel.modules.security.service.TokenProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class RetelBootSecurityConfig extends WebSecurityConfigurerAdapter {

    private final TokenProviderService tokenProviderService;
    private final CorsFilter corsFilter;
    private final JwtAuthenticationEntryPoint authenticationErrorHandler;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final ApplicationContext applicationContext;


    public RetelBootSecurityConfig(TokenProviderService tokenProviderService,
                                   CorsFilter corsFilter,
                                   JwtAuthenticationEntryPoint authenticationErrorHandler,
                                   JwtAccessDeniedHandler jwtAccessDeniedHandler,
                                   ApplicationContext applicationContext) {
        this.tokenProviderService = tokenProviderService;
        this.corsFilter = corsFilter;
        this.authenticationErrorHandler = authenticationErrorHandler;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.applicationContext = applicationContext;

    }

    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }


    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // 去除 ROLE_ 前缀
        return new GrantedAuthorityDefaults("");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 密码加密方式
        //return new BCryptPasswordEncoder();
        //ldap加密方式
        return  new SSHAPasswordEncoder();

    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new RatelAuthenticationProvider();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // 搜寻匿名标记 url： @AnonymousAccess
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class).getHandlerMethods();
        Set<String> anonymousUrls = new HashSet<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> infoEntry : handlerMethodMap.entrySet()) {
            HandlerMethod handlerMethod = infoEntry.getValue();
            AnonymousAccess anonymousAccess = handlerMethod.getMethodAnnotation(AnonymousAccess.class);
            if (null != anonymousAccess) {
                anonymousUrls.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
            }
        }
        httpSecurity
                // 禁用 CSRF
                .csrf().disable()
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                // 授权异常
                .exceptionHandling()
                .authenticationEntryPoint(authenticationErrorHandler)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                // 防止iframe 造成跨域
                .and()
                .headers()
                .frameOptions()
                .disable()

                // 不创建会话
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests()
                .antMatchers("/index.html").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 静态资源等等
                .antMatchers(
                        HttpMethod.GET,
                        "/*.html",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.json",
                        "/component/**",
                        "/src/**",
                        "/webSocket/**"
                ).permitAll()
                // swagger 文档
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/*/api-docs").permitAll()
                .antMatchers("/auth/login").permitAll()
                //
                .antMatchers("/oauth/enterprise/**").permitAll()
                .antMatchers("/oauth/enterpriseUser/**").permitAll()
                //ldap同步用户部门
                .antMatchers("/api/ladp/**").permitAll()
                //qywx同步用户部门
                .antMatchers("/api/qywx/**").permitAll()


                .antMatchers("/auth/login").permitAll()
                .antMatchers("/vx/login").permitAll()
                .antMatchers("/oauth/token").permitAll()
                .antMatchers("/oauth/authorize").permitAll()
                .antMatchers("/webSocket/*").permitAll()
                .antMatchers("/api/wx/auth").permitAll()
                .antMatchers("/api/wx/auth/toutiao").permitAll()
                .antMatchers("/api/wx/auth/common").permitAll()
                .antMatchers("/api/wx/auth/qq").permitAll()
                .antMatchers("/api/wx/userSetting/getUsers").permitAll()
                // 文件
                .antMatchers("/static/**").permitAll()
                .antMatchers("/avatar/**").permitAll()
                .antMatchers("/file/**").permitAll()
                // 阿里巴巴 druid
                .antMatchers("/druid/**").permitAll()
                // 放行OPTIONS请求
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 自定义匿名访问所有url放行 ： 允许匿名和带权限以及登录用户访问
                .antMatchers(anonymousUrls.toArray(new String[0])).permitAll()
                // 所有请求都需要认证
                .anyRequest().authenticated()
                .and().apply(securityConfigurerAdapter());
    }

    private TokenConfigurer securityConfigurerAdapter() {
        return new TokenConfigurer(tokenProviderService);
    }

}
