/*
 * Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jahia.modules.jahiacsrfguard.filters;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.modules.jahiacsrfguard.Config;
import org.jahia.modules.jahiacsrfguard.token.SessionTokenHolder;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.owasp.csrfguard.CsrfGuardFilter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper for servlet filter: CsrfGuardFilter
 */
public class CsrfGuardServletFilterWrapper extends AbstractServletFilter {
    private CsrfGuardFilter csrfGuardFilter;
    private Set<Config> configs = new HashSet<>();
    private CommonsMultipartResolver multipartResolver = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        csrfGuardFilter = new CsrfGuardFilter();
        csrfGuardFilter.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        SessionTokenHolder.setCurrentRequest((HttpServletRequest)request);
        setRequestUser(request);

        try {
            if (isFiltered(request) && !isWhiteListed(request)) {
                if (request instanceof HttpServletRequest) {
                    HttpServletRequest httpRequest = (HttpServletRequest) request;
                    request = !ServletFileUpload.isMultipartContent(httpRequest) ? request
                            : multipartResolver.resolveMultipart(new MultiReadHttpServletRequest(httpRequest));
                }
                csrfGuardFilter.doFilter(request, response, chain);
                return;
            }

            chain.doFilter(request, response);
        } finally {
            SessionTokenHolder.setCurrentRequest(null);
        }
    }

    private void setRequestUser(ServletRequest request) {
        JahiaUser currentUser = JCRSessionFactory.getInstance().getCurrentUser();
        if (currentUser != null) {
            request.setAttribute("REMOTE_USER", currentUser.getName());
        }
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

    public void setMultipartResolver(CommonsMultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }
}
