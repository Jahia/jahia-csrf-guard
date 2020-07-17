package org.jahia.modules.jahiacsrfguard.filters;

import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.modules.jahiacsrfguard.Config;
import org.owasp.csrfguard.CsrfGuardFilter;

import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsrfGuardServletFilterWrapper extends AbstractServletFilter {
    private CsrfGuardFilter csrfGuardFilter;
    private List<Config> configs = new ArrayList<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        csrfGuardFilter = new CsrfGuardFilter();
        csrfGuardFilter.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (isFiltered(request) && !isWhiteListed(request)) {
            csrfGuardFilter.doFilter(request, response, chain);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        csrfGuardFilter.destroy();
    }

    public void registerConfig(Config config) {
        configs.add(config);
    }

    public void unregisterConfig(Config config) {
        configs.remove(config);
    }

    public boolean isFiltered(ServletRequest request) {
        return configs.stream().anyMatch(config -> config.isFiltered(request));
    }

    public boolean isWhiteListed(ServletRequest request) {
        return configs.stream().anyMatch(config -> config.isWhiteListed(request));
    }
}
