package com.ratel.config;


import com.ratel.framework.config.RatelRepositoryFactoryBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"com.ratel"},
        repositoryFactoryBeanClass = RatelRepositoryFactoryBean.class//指定自己的工厂类
)
public class RatelRepositoryConfig {

}
