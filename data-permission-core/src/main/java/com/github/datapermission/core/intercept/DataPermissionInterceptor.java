package com.github.datapermission.core.intercept;

import com.github.datapermission.core.context.DataPermissionLocalSwitch;
import com.github.datapermission.core.handler.PermissionHandler;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 数据权限处理插件
 *
 * @author keguang
 */
@Intercepts(
    {
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
    }
)
public class DataPermissionInterceptor implements Interceptor {
    
    private PermissionHandler permissionHandler;
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 判断本地线程控制开关是否禁用了数据权限插件
        if (DataPermissionLocalSwitch.isDisable()) {
            return invocation.proceed();
        }
        try {
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args[1];
            RowBounds rowBounds = (RowBounds) args[2];
            ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];
            Executor executor = (Executor) invocation.getTarget();
            CacheKey cacheKey;
            BoundSql boundSql;
            if (args.length == 4) {
                boundSql = ms.getBoundSql(parameter);
                cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
            } else {
                cacheKey = (CacheKey) args[4];
                boundSql = (BoundSql) args[5];
            }
            // 判断是否需要进行权限处理
            if (permissionHandler.skipPermissionCtrl(ms, parameter, boundSql)) {
                return invocation.proceed();
            }
            // 进行数据权限处理操作
            return permissionHandler.handleWithPermission(executor, ms, parameter, rowBounds, resultHandler,
                    cacheKey, boundSql);
        } finally {
            permissionHandler.doFinally();
        }
    }
    
    public void setPermissionHandler(PermissionHandler permissionHandler) {
        this.permissionHandler = permissionHandler;
    }
    
}
