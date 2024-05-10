package com.github.datapermission.web.config;

import com.github.datapermission.web.filter.DataPermissionFilter;
import com.github.datapermission.web.filter.DefaultDataPermissionFilter;
import com.github.datapermission.web.spi.PermissionObtainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据权限插件webFilter配置类
 * 
 * @author keguang
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(WebMvcAutoConfiguration.class)
@ConditionalOnProperty(prefix = "data.permission", name = "web-filter-enabled", havingValue = "true")
public class WebFilterConfiguration {
    
    @Bean
    @ConditionalOnSingleCandidate(PermissionObtainer.class)
    @ConditionalOnMissingFilterBean
    public FilterRegistrationBean<DataPermissionFilter> defaultDataPermissionFilter(
            PermissionObtainer permissionObtainer) {
        
        FilterRegistrationBean<DataPermissionFilter> registration = new FilterRegistrationBean<>();
        registration.setName("defaultDataPermissionFilter");
        registration.setFilter(new DefaultDataPermissionFilter(permissionObtainer));
        return registration;
    }
    
}
