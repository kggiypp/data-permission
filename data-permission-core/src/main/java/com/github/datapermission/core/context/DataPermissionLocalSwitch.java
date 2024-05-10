package com.github.datapermission.core.context;

/**
 * 数据权限本地线程控制开关
 * 
 * <p> 支持的应用场景：一次请求线程中，有一部分程序代码不需要数据权限控制，其它部分需要，
 * 灵活控制代码块之间的数据权限开关。
 * For example:
 * <pre class="code">
 * // (do something ...) 需要数据权限控制
 * DataPermissionLocalSwitch.disable();
 * // (do something ...) 不需要数据权限控制
 * DataPermissionLocalSwitch.endDisable();
 * // (do something ...) 需要数据权限控制
 * </pre>
 * 
 * @author keguang
 * @date 2023/12/24 14:27
 */
public class DataPermissionLocalSwitch {
    
    private static final ThreadLocal<Object> LOCAL_SWITCH = new InheritableThreadLocal<>();
    
    private static final Object FLAG = new Object();
    
    public static void disable() {
        LOCAL_SWITCH.set(FLAG);
    }
    
    public static void endDisable() {
        LOCAL_SWITCH.set(null);
    }
    
    public static boolean isDisable() {
        return LOCAL_SWITCH.get() != null;
    }
    
}
