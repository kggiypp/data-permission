package com.github.datapermission.core.handler;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 不做数据权限处理的handler类
 * 
 * @author keguang
 */
@SuppressWarnings("unused")
public class NoPermissionHandler implements PermissionHandler {
    
    @Override
    public boolean skipPermissionCtrl(MappedStatement ms, Object parameterObject, BoundSql boundSql) {
        return true;
    }

    @Override
    public Object handleWithPermission(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
            ResultHandler<?> resultHandler, CacheKey cacheKey, BoundSql boundSql) throws Exception {
        
        throw new UnsupportedOperationException("不支持权限处理");
    }
    
}
