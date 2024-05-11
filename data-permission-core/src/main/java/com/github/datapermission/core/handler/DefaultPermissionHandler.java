package com.github.datapermission.core.handler;

import com.github.datapermission.core.context.DataPermissionContext;
import com.github.datapermission.core.exception.PermissionsNotSetException;
import com.github.datapermission.core.parser.PermissionColumnParser;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import static org.apache.ibatis.scripting.xmltags.ForEachSqlNode.ITEM_PREFIX;

/**
 * 默认的数据权限处理实现类
 * 
 * <p>根据sql里的候选权限字段（例如：dept_id）来进行数据隔离。
 * 如果sql没有查询权限字段，则不做任何操作，对原sql没任何影响。
 * 
 * <p>不要使用通配符 select * 来查询字段！
 * 如果确实有很多层from子句，外层查询可以使用通配符，但不要所有内层全都使用通配符，
 * 例如：select t.* from (select dept_id, col2, col3 from ...) t
 * 
 * <p>如果所有from子句里都使用通配符查询，则无法通过sql判断出是否查询了dept_id字段，也就不执行数据权限插件处理了，
 * 例如：select t.* from (select * from ...) t
 * 这种情况就无法执行数据权限隔离插件了
 * 
 * @author keguang
 */
public class DefaultPermissionHandler extends AbstractCommonHandler {

    private static final String[] DEFAULT_CANDIDATE_COLUMNS = {"dept_id", "DEPT_ID", "deptId"};
    
    private static final ThreadLocal<String> LOCAL_PERMISSION_COLUMN = new ThreadLocal<>();
    
    @Override
    public boolean skipPermissionCtrl(MappedStatement ms, Object parameterObject, BoundSql boundSql) {
        if (super.skipPermissionCtrl(ms, parameterObject, boundSql)) {
            return true;
        }
        
        PermissionColumnParser parser = CollectionUtils.isEmpty(candidatePermissionColumns) ?
                new PermissionColumnParser(DEFAULT_CANDIDATE_COLUMNS) :
                new PermissionColumnParser(candidatePermissionColumns);
        
        String permissionColumn = parser.extractPermissionColumn(boundSql.getSql());
        if (permissionColumn == null) {
            return true;
        }
        LOCAL_PERMISSION_COLUMN.set(permissionColumn);
        return false;
    }
    
    @Override
    public Object handleWithPermission(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
            ResultHandler<?> resultHandler, CacheKey cacheKey, BoundSql boundSql) throws Exception {
        
        Collection<String> permissionValues = DataPermissionContext.getPermissionValues();
        if (CollectionUtils.isEmpty(permissionValues)) {
            throw new PermissionsNotSetException("当前请求线程中未设置权限插件需要的权限值集合");
        }
        
        Set<String> permissionSet = permissionValues instanceof Set ?
                (Set<String>) permissionValues : new HashSet<>(permissionValues);
        String permissionCloumn = LOCAL_PERMISSION_COLUMN.get();
        // 处理权限值参数，修改BoundSql附加额外参数，以及parameterMappings
        processPermissionParameter(ms, boundSql, permissionSet, cacheKey, permissionCloumn);
        // 重新构造sql，原始sql加上权限过滤条件
        rebuildSqlWithPermission(ms, boundSql, permissionSet, permissionCloumn);
        return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }
    
    @Override
    public void doFinally() {
        LOCAL_PERMISSION_COLUMN.remove();
    }

    /**
     * 修改boundsql附加额外的参数值，以及新增相应的parameterMappings
     * @param ms ms
     * @param boundSql boundSql
     * @param permissionValues 权限值集合
     * @param cacheKey cacheKey
     */
    private void processPermissionParameter(MappedStatement ms, BoundSql boundSql, Set<String> permissionValues,
            CacheKey cacheKey, String permissionCloumn) {
        
        List<ParameterMapping> parameterMappings = new ArrayList<>(boundSql.getParameterMappings());
        int index = 0;
        for (String permissionValue : permissionValues) {
            String itemName = itemizeItem(permissionCloumn, index++);
            boundSql.setAdditionalParameter(itemName, permissionValue);
            parameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), itemName, String.class).build());
        }
        MetaObject metaObject = ms.getConfiguration().newMetaObject(boundSql);
        metaObject.setValue("parameterMappings", parameterMappings);
        cacheKey.update(permissionValues);
    }

    /**
     * 重新构建sql，原始sql加上权限过滤条件
     * @param boundSql boundSql
     * @param permissionValues 权限值集合
     * @param permissionCloumn 权限字段
     */
    private void rebuildSqlWithPermission(MappedStatement ms, BoundSql boundSql, Set<String> permissionValues,
            String permissionCloumn) {
        
        // 拼接占位符
        StringJoiner placeholderJoiner = new StringJoiner(", ");
        for (int i = 0; i < permissionValues.size(); i++) {
            placeholderJoiner.add("?");
        }
        // 重新构造sql
        String sql = boundSql.getSql();
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 50 + permissionValues.size() * 3);
        sqlBuilder.append("select t.* from ( \n");
        sqlBuilder.append(sql);
        sqlBuilder.append("\n ) t \n");
        sqlBuilder.append("where t.").append(permissionCloumn).append(" in (").append(placeholderJoiner).append(")");
        String newSql = sqlBuilder.toString();
        MetaObject metaObject = ms.getConfiguration().newMetaObject(boundSql);
        metaObject.setValue("sql", newSql);
    }
    
    private static String itemizeItem(String item, int i) {
        // 致敬mybatis
        return ITEM_PREFIX + item + "_" + i;
    }
    
}
