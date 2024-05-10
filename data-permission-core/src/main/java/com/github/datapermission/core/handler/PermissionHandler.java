package com.github.datapermission.core.handler;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * 数据权限处理标准接口
 * 
 * @author keguang
 */
public interface PermissionHandler {
    
    /**
     * 判断是否跳过数据权限控制
     * @param ms ms
     * @param parameterObject parameterObject
     * @param boundSql boundSql
     * @return true or false
     */
    boolean skipPermissionCtrl(MappedStatement ms, Object parameterObject, BoundSql boundSql);

    /**
     * 进行数据权限处理
     * @param executor executor
     * @param ms ms
     * @param parameter parameter
     * @param rowBounds rowBounds
     * @param resultHandler resultHandler
     * @param cacheKey cacheKey
     * @param boundSql boundSql
     * @return 处理结果
     * @throws Exception exception
     */
    Object handleWithPermission(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
            ResultHandler<?> resultHandler, CacheKey cacheKey, BoundSql boundSql) throws Exception;

    /**
     * finally处理操作
     */
    default void doFinally() {}

    /**
     * 预留属性扩展方法
     * @param properties properties
     */
    default void setProperties(Properties properties) {}
    
}
