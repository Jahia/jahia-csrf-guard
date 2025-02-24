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
 *     Copyright (C) 2002-2025 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.modules.jahiacsrfguard;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.util.*;

/**
 * Global OSGI config for Jahia CSRF Guard.
 * @author Jerome Blanchard
 */
@Component(immediate = true, service = JahiaCsrfGuardGlobalConfig.class, property = "service.pid=org.jahia.modules.jahiacsrfguard.global", configurationPid = "org.jahia.modules.jahiacsrfguard.global")
public class JahiaCsrfGuardGlobalConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(JahiaCsrfGuardGlobalConfig.class.getName());

    public static final String ENABLED = "jahia.csrf-guard.enabled";
    public static final String SERVLET_ALIAS = "jahia.csrf-guard.servletAlias";
    public static final String BYPASS_FOR_GUEST = "jahia.csrf-guard.bypassForGuest";
    public static final String RESOLVED_URL_PATTERNS = "jahia.csrf-guard.resolvedUrlPatterns";

    private Map<String, String> config = new HashMap<>();
    private boolean enabled = true;
    private String servletAlias = "";
    private String servletPath = "";
    private boolean bypassForGuest = true;
    private List<String> resolvedUrlPatterns = new ArrayList<>();
    private MultipartResolver multipartResolver;

    @Activate
    @Modified
    public void modified(Map<String, String> config) {
        LOGGER.debug("Updating Jahia CSRF Guard Global configuration, config size: {}", config.size());
        this.setConfig(config);
        this.setEnabled(config.getOrDefault(ENABLED, "true").equalsIgnoreCase("true"));
        this.setServletAlias(config.getOrDefault(SERVLET_ALIAS, "/CsrfServlet"));
        this.setServletPath("/modules".concat(this.getServletAlias()));
        this.setBypassForGuest(config.getOrDefault(BYPASS_FOR_GUEST, "true").equalsIgnoreCase("true"));
        this.setResolvedUrlPatterns(config.getOrDefault(RESOLVED_URL_PATTERNS, "").isEmpty() ?
                new ArrayList<>() :
                List.of(config.get(RESOLVED_URL_PATTERNS).split(",")));
        this.setMultipartResolver(new CommonsMultipartResolver());
        LOGGER.debug("Updated Jahia CSRF Guard Global configuration: {}", this);
    }

    public String get(String configKey) {
        return this.config.get(configKey);
    }

    public int getSize() {
        return this.config.size();
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServletAlias() {
        return servletAlias;
    }

    public void setServletAlias(String servletAlias) {
        this.servletAlias = servletAlias;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public boolean bypassForGuest() {
        return bypassForGuest;
    }

    public void setBypassForGuest(boolean bypassForGuest) {
        this.bypassForGuest = bypassForGuest;
    }

    public List<String> getResolvedUrlPatterns() {
        return resolvedUrlPatterns;
    }

    public void setResolvedUrlPatterns(List<String> resolvedUrlPatterns) {
        this.resolvedUrlPatterns = resolvedUrlPatterns;
    }

    public MultipartResolver getMultipartResolver() {
        return multipartResolver;
    }

    public void setMultipartResolver(MultipartResolver multipartResolver) {
        this.multipartResolver = multipartResolver;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JahiaCsrfGuardGlobalConfig that = (JahiaCsrfGuardGlobalConfig) o;
        return enabled == that.enabled && bypassForGuest == that.bypassForGuest && Objects.equals(servletAlias, that.servletAlias)
                && Objects.equals(servletPath, that.servletPath) && Objects.equals(resolvedUrlPatterns, that.resolvedUrlPatterns)
                && Objects.equals(multipartResolver, that.multipartResolver);
    }

    @Override public int hashCode() {
        return Objects.hash(enabled, servletAlias, servletPath, bypassForGuest, resolvedUrlPatterns, multipartResolver);
    }

    @Override public String toString() {
        return "JahiaCsrfGuardGlobalConfig{" + "enabled=" + enabled + ", servletPath='" + servletPath + '\'' + ", bypassForGuest="
                + bypassForGuest + ", resolvedUrlPatterns=" + resolvedUrlPatterns + '}';
    }
}
