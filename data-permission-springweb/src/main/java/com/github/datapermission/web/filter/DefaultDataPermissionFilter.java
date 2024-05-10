package com.github.datapermission.web.filter;

import com.github.datapermission.core.context.DataPermissionLocalSwitch;
import com.github.datapermission.web.spi.PermissionObtainer;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * 默认的数据权限插件过滤器
 * 
 * @author keguang
 */
public class DefaultDataPermissionFilter extends OncePerRequestFilter implements DataPermissionFilter {
    
    private PermissionObtainer permissionObtainer;
    
    public DefaultDataPermissionFilter(PermissionObtainer permissionObtainer) {
        this.permissionObtainer = permissionObtainer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        DataPermissionFilter.super.doFilter(request, response, filterChain);
    }

    @Override
    public Collection<String> getPermissions(HttpServletRequest request) {
        try {
            DataPermissionLocalSwitch.disable();
            return permissionObtainer.obtainPermissions(request);
        } finally {
            DataPermissionLocalSwitch.endDisable();
        }
    }
    
}
