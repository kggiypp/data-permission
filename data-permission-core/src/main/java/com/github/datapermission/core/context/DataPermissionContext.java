package com.github.datapermission.core.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据权限插件上下文
 * 
 * @author keguang
 * @date 2024/4/22 15:19
 */
public class DataPermissionContext {

    private static final ThreadLocal<Map<Object, Object>> CONTEXT = new InheritableThreadLocal<Map<Object, Object>>() {
        
        @Override
        protected Map<Object, Object> initialValue() {
            return new HashMap<>();
        }
    };
    
    private static final String PERMISSION_VALUES = "permissionValues";

    public static Map<Object, Object> getContext() {
        return CONTEXT.get();
    }
    
    public static void setPermissionValues(Collection<String> permissionValues) {
        put(PERMISSION_VALUES, permissionValues);
    }
    
    public static Collection<String> getPermissionValues() {
        return get(PERMISSION_VALUES);
    }
    
    public static void put(Object key, Object value) {
        getContext().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Object key) {
        return (T) getContext().get(key);
    }
    
    public static void release() {
        CONTEXT.remove();
    }
    
}
