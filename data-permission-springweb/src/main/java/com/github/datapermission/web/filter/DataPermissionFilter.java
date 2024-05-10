package com.github.datapermission.web.filter;

import com.github.datapermission.core.context.DataPermissionContext;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;

/**
 * 标准数据权限过滤器接口
 * 
 * @author keguang
 */
public interface DataPermissionFilter extends Filter {

    /**
     * 获取当前请求线程的用户权限集合
     * @param request httpServletRequest对象
     * @return 权限集合
     */
    Collection<String> getPermissions(HttpServletRequest request);

    /**
     * {@inheritDoc}
     */
    @Override
    default void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            DataPermissionContext.setPermissionValues(getPermissions((HttpServletRequest) request));
            chain.doFilter(request, response);
        } finally {
            DataPermissionContext.release();
        }
    }
    
}
