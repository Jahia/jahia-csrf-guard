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
import org.jahia.modules.jahiacsrfguard.JahiaCsrfGuardConfig;
import org.jahia.modules.jahiacsrfguard.JahiaCsrfGuardConfigFactory;
import org.jahia.modules.jahiacsrfguard.JahiaCsrfGuardGlobalConfig;
import org.jahia.modules.jahiacsrfguard.token.SessionTokenHolder;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.owasp.csrfguard.CsrfGuardFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

/**
 * Wrapper for servlet filter: CsrfGuardFilter
 */
@Component(immediate = true, service = AbstractServletFilter.class)
public class CsrfGuardServletFilterWrapper extends AbstractServletFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrfGuardServletFilterWrapper.class);

    private volatile Collection<JahiaCsrfGuardConfig> configs = new HashSet<>();
    private volatile JahiaCsrfGuardGlobalConfig globalConfig;
    private CsrfGuardFilter csrfGuardFilter;

    @Activate
    public void activate() {
        LOGGER.info("Started Jahia CSRF Guard Servlet Filter Wrapper");
        setFilterName("Jahia CSRF Guard Servlet Filter Wrapper");
        setMatchAllUrls(true);
        setUrlPatterns(new String[]{"/*"});
    }

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
            if (isGlobalConfigEnabled() && isFiltered(request) && !isWhiteListed(request)) {
                if (request instanceof HttpServletRequest) {
                    HttpServletRequest httpRequest = (HttpServletRequest) request;
                    request = !ServletFileUpload.isMultipartContent(httpRequest) ? request
                            : globalConfig.getMultipartResolver().resolveMultipart(new MultiReadHttpServletRequest(httpRequest));
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

    public JahiaCsrfGuardGlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    @Reference(service = JahiaCsrfGuardConfigFactory.class, policy = ReferencePolicy.DYNAMIC, bind = "setConfigs", unbind = "clearConfigs")
    public void setConfigs(JahiaCsrfGuardConfigFactory configFactory) {
        LOGGER.info("Setting configurations from factory");
        this.configs = configFactory.getConfigs();
    }

    public void clearConfigs(JahiaCsrfGuardConfigFactory configFactory) {
        LOGGER.info("Clearing configurations from factory");
        this.configs = new HashSet<>();
    }

    @Reference(service = JahiaCsrfGuardGlobalConfig.class, policy = ReferencePolicy.DYNAMIC, updated = "setGlobalConfig", unbind = "unsetGlobalConfig")
    public void setGlobalConfig(JahiaCsrfGuardGlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public void unsetGlobalConfig(JahiaCsrfGuardGlobalConfig globalConfig) {
        this.globalConfig = null;
    }

    public boolean isGlobalConfigEnabled() {
        if (globalConfig == null) {
            LOGGER.warn("Global configuration is not set, unable to apply CSRF Guard filter");
            return false;
        }
        return globalConfig.isEnabled();
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
