package com.github.datapermission.core.handler;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Properties;

/**
 * 数据权限控制通用公共逻辑的抽象父类
 * 
 * @author keguang
 * @date 2023/12/23 16:25
 */
public abstract class AbstractCommonHandler implements PermissionHandler {
    
    public static final String WILDCARD = "*";
    
    protected List<String> excludeMappedStatementIds;

    protected List<String> candidatePermissionColumns;
    
    protected Properties properties;

    /**
     * 需要被排除的不执行插件的MappedStatement集合
     * @param excludeMappedStatementIds
     */
    public void setExcludeMappedStatementIds(List<String> excludeMappedStatementIds) {
        this.excludeMappedStatementIds = excludeMappedStatementIds;
    }

    /**
     * 权限候选字段集合，集合内靠前的候选字段优先匹配
     * @param candidatePermissionColumns
     */
    public void setCandidatePermissionColumns(List<String> candidatePermissionColumns) {
        this.candidatePermissionColumns = candidatePermissionColumns;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    @Override
    public boolean skipPermissionCtrl(MappedStatement ms, Object parameterObject, BoundSql boundSql) {
        return isExcludeMappedStatement(ms);
    }

    private boolean isExcludeMappedStatement(MappedStatement ms) {
        if (CollectionUtils.isEmpty(excludeMappedStatementIds)) {
            return false;
        }
        String msId = ms.getId();
        String namespace = msId.substring(0, msId.lastIndexOf('.'));
        String statementId = msId.substring(msId.lastIndexOf('.') + 1);
        // 处理msId匹配情况，考虑到可能使用通配符，先匹配namespace前缀，再匹配statementId
        for (String excludeMsId : excludeMappedStatementIds) {
            if (excludeMsId.startsWith(namespace)) {
                String excludeId = excludeMsId.substring(excludeMsId.lastIndexOf('.') + 1);
                if (WILDCARD.equals(excludeId) || statementId.equals(excludeId)) {
                    return true;
                }
            }
        }
        return false;
    }
    
}
