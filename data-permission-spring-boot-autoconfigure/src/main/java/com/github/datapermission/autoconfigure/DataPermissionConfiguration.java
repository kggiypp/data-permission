package com.github.datapermission.autoconfigure;

import com.github.datapermission.banner.DataPermissionBanner;
import com.github.datapermission.core.handler.DefaultPermissionHandler;
import com.github.datapermission.core.handler.PermissionHandler;
import com.github.datapermission.core.intercept.DataPermissionInterceptor;
import com.github.datapermission.web.config.WebFilterConfiguration;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * 数据权限启动配置类，默认会开启自动装配
 * 
 * <p>如果需要关闭使用，可在spring配置文件指定 {@code data.permission.enabled=false}
 * 
 * @author keguang
 * @date 2023/12/21 11:18
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(SqlSessionFactory.class)
@ConditionalOnProperty(prefix = "data.permission", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(DataPermissionProperties.class)
@AutoConfigureAfter({MybatisAutoConfiguration.class, PageHelperAutoConfiguration.class})
@Import(WebFilterConfiguration.class)
public class DataPermissionConfiguration implements InitializingBean {

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;
    
    @Autowired
    private DataPermissionInterceptor dataPermissionInterceptor;
    
    @Autowired
    private DataPermissionProperties dataPermissionProperties;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        sqlSessionFactoryList.forEach(ssf -> ssf.getConfiguration().addInterceptor(dataPermissionInterceptor));

        Boolean bannerEnabled = dataPermissionProperties.getBannerEnabled();
        if (bannerEnabled == null || bannerEnabled) {
            // 打印骚Banner
            DataPermissionBanner.printBanner(System.out);
        }
    }
    
    @Bean
    public DataPermissionInterceptor dataPermissionInterceptor(PermissionHandler permissionHandler) {
        DataPermissionInterceptor interceptor = new DataPermissionInterceptor();
        interceptor.setPermissionHandler(permissionHandler);
        return dataPermissionInterceptor;
    }
    
    @Bean
    @ConditionalOnMissingBean
    public PermissionHandler defaultPermissionHandler() {
        DefaultPermissionHandler defaultHandler = new DefaultPermissionHandler();
        defaultHandler.setExcludeMappedStatementIds(dataPermissionProperties.getExcludeMappedStatementIds());
        defaultHandler.setCandidatePermissionColumns(dataPermissionProperties.getCandidatePermissionColumns());
        defaultHandler.setProperties(dataPermissionProperties.getProperties());
        return defaultHandler;
    }
    
}
