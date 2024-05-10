package com.github.datapermission.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Properties;

/**
 * 数据权限配置文件
 * 
 * @author keguang
 * @date 2024/4/12 10:09
 */
@Data
@ConfigurationProperties(prefix = "data.permission")
public class DataPermissionProperties {

    /**
     * 是否开启数据权限插件（默认开启）
     */
    private Boolean enabled;
    
    /**
     * 自定义的扩展属性配置
     * 例：data.permission.properties.key=value
     */
    private Properties properties;

    /**
     * 不执行数据权限插件的MappedStatementId（namespace + id）等同于（Mapper类全限定名 + 方法名），
     * id可使用通配符 * 表示该mapper所有方法都排除执行插件。
     * 
     * <p>application.properties里可按如下格式配置：
     * 
     * <pre class="code">
     * data.permission.exclude-mapped-statement-ids=\
     * com.xxx.mapper.XxxMapper1.*,\
     * com.xxx.mapper.XxxMapper2.id1,\
     * com.xxx.mapper.XxxMapper2.id2,\
     * </pre>
     * 
     */
    private List<String> excludeMappedStatementIds;

    /**
     * 指定的候选权限字段集合，当sql查询字段里包含了该字段时就会自动执行数据权限插件了，
     * 一般常规的权限字段比如：dept_id,role_id,tenant_id等，配置中越靠前的字段越优先匹配，
     * <strong>配置区分大小写</strong>
     * 
     * <p>application.properties里可按如下格式配置：
     * 
     * <pre class="code">
     * data.permission.candidate-permission-columns=dept_id,role_id,tenant_id
     * </pre>
     */
    private List<String> candidatePermissionColumns;

    /**
     * 是否开启数据权限插件web过滤器配置
     */
    private Boolean webFilterEnabled;

    /**
     * 是否开启数据权限插件logo打印（默认开启）
     */
    private Boolean bannerEnabled;
    
}
