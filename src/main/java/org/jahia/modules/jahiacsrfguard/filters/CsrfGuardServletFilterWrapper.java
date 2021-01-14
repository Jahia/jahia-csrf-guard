/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2021 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms & Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.modules.jahiacsrfguard.filters;

import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.modules.jahiacsrfguard.Config;
import org.owasp.csrfguard.CsrfGuardFilter;

import javax.servlet.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper for servlet filter: CsrfGuardFilter
 */
public class CsrfGuardServletFilterWrapper extends AbstractServletFilter {
    private CsrfGuardFilter csrfGuardFilter;
    private Set<Config> configs = new HashSet<>();

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

    /**
     * Regsiter/add configuration to filter
     * @param config configurationn object
     */
    public void registerConfig(Config config) {
        configs.add(config);
    }

    /**
     * Remove conifguration from filter
     * @param config configurationn object
     */
    public void unregisterConfig(Config config) {
        configs.remove(config);
    }

    /**
     * Check all url pattern configurations to see whether CsrfGuardFilter should be applied on current request
     * @param request client request object for servlet
     * @return true if CsrfGuardFilter should be applied
     */
    public boolean isFiltered(ServletRequest request) {
        return configs.stream().anyMatch(config -> config.isFiltered(request));
    }

    /**
     * Check all whitelist configurations to see whether CsrfGuardFilter should not be applied on current request
     * @param request client request object for servlet
     * @return true if URL is whitelisted for CsrfGuardFilter, so it should not be applied
     */
    public boolean isWhiteListed(ServletRequest request) {
        return configs.stream().anyMatch(config -> config.isWhiteListed(request));
    }
}
