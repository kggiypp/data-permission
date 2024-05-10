package com.github.datapermission.web.spi;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * 获取权限集合标准接口
 * 
 * 借鉴spi思想，该实现类逻辑由使用者自己完成，该实现类需要添加至spring容器里
 * 
 * @author keguang
 */
public interface PermissionObtainer {

    /**
     * 获取权限集合
     * @param request httpServletRequest对象
     * @return 权限集合
     */
    Collection<String> obtainPermissions(HttpServletRequest request);
    
}
