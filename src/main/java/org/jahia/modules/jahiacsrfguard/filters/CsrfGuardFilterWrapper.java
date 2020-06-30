package org.jahia.modules.jahiacsrfguard.filters;

import org.jahia.bin.filters.AbstractServletFilter;
import org.owasp.csrfguard.CsrfGuardFilter;

import javax.servlet.*;
import java.io.IOException;

public class CsrfGuardFilterWrapper extends AbstractServletFilter {
    private CsrfGuardFilter csrfGuardFilter;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        csrfGuardFilter = new CsrfGuardFilter();
        csrfGuardFilter.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        csrfGuardFilter.doFilter(request, response, chain);
    }

    @Override
    public void destroy() {
        csrfGuardFilter.destroy();
    }
}
