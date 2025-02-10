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

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.owasp.csrfguard.servlet.JavaScriptServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;

/**
 * @author Jerome Blanchard
 */
@Component(service = JahiaCsrfGuardService.class, immediate = true)
public class JahiaCsrfGuardService {

    public static final Logger LOGGER = LoggerFactory.getLogger(JahiaCsrfGuardService.class);

    private boolean registered = false;
    private String registeredAlias = "";

    @Reference
    protected HttpService httpService;

    private JahiaCsrfGuardGlobalConfig config;

    public JahiaCsrfGuardService() {
        LOGGER.info("Building Jahia CSRF Guard service...");
    }

    @Activate
    public void activate(BundleContext context) throws Exception {
        LOGGER.info("Jahia CSRF Guard service starting");
        this.handleConfigChange();
    }

    @Deactivate
    public void stop(BundleContext context) throws Exception {
        LOGGER.info("Jahia CSRF Guard service stopping");
    }

    @Reference(service = JahiaCsrfGuardGlobalConfig.class, policy = ReferencePolicy.DYNAMIC, updated = "setConfig")
    private void setConfig(JahiaCsrfGuardGlobalConfig config) throws ServletException, NamespaceException {
        this.config = config;
        if (httpService == null ) {
            LOGGER.info("Jahia CSRF Guard config with {} entries did not update the service as not fully started yet.", config.getSize());
            return;
        }
        this.handleConfigChange();
    }

    private void unsetConfig(JahiaCsrfGuardGlobalConfig config) {
        this.config = null;
    }

    private void handleConfigChange() throws ServletException, NamespaceException {
        if (registered) {
            httpService.unregister(registeredAlias);
            registered = false;
            registeredAlias = "";
        }
        if (config.isEnabled()) {
            LOGGER.info("Enabling Jahia CSRF Guard service...");
            httpService.registerServlet(config.getServletPath(), new JavaScriptServlet(), null, null);
            registered = true;
            registeredAlias = config.getServletPath();
        } else {
            LOGGER.info("Jahia CSRF Guard service is disabled");
        }
    }
}
