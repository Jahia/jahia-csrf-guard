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

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.osgi.service.component.annotations.*;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.owasp.csrfguard.CsrfGuardServletContextListener;
import org.owasp.csrfguard.servlet.JavaScriptServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

/**
 * @author Jerome Blanchard
 */
@Component(service = JahiaCsrfGuardService.class, immediate = true)
public class JahiaCsrfGuardService {

    public static final Logger LOGGER = LoggerFactory.getLogger(JahiaCsrfGuardService.class);

    @Reference
    protected HttpService httpService;

    @Reference(service = JahiaCsrfGuardGlobalConfig.class, cardinality = ReferenceCardinality.MANDATORY)
    private JahiaCsrfGuardGlobalConfig config;

    private String registeredAlias = "";

    public JahiaCsrfGuardService() {
        LOGGER.debug("Building Jahia CSRF Guard service...");
    }

    @Activate
    public void activate() {
        LOGGER.info("Jahia CSRF Guard service starting");
        this.unregister();
        if (config.isEnabled()) {
            this.register();
        } else {
            LOGGER.info("Jahia CSRF Guard service is disabled");
        }
    }

    @Deactivate
    public void stop() {
        LOGGER.info("Jahia CSRF Guard service stopping");
        this.unregister();
    }

    private void unregister() {
        if (StringUtils.isNotBlank(registeredAlias)) {
            LOGGER.info("Unregistering Jahia CSRF Guard JavaScriptServlet...");
            try {
                httpService.unregister(registeredAlias);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Error while unregistering Jahia CSRF Guard JavaScriptServlet", e);
            }
            registeredAlias = "";
        }
    }

    private void register() {
        LOGGER.info("Registering Jahia CSRF Guard JavaScriptServlet...");
        if (httpService == null ) {
            LOGGER.error("Unable to register servlet as HttpService is not available yet.");
            return;
        }
        try {
            httpService.registerServlet(config.getServletAlias(), new JavaScriptServlet(), null, null);
            registeredAlias = config.getServletAlias();
            CsrfGuardServletContextListener csrfGuardServletContextListener = new CsrfGuardServletContextListener();
            csrfGuardServletContextListener.contextInitialized(new ServletContextEvent(JahiaContextLoaderListener.getServletContext()));
            LOGGER.info("Jahia CSRF Guard JavaScriptServlet registered at path: {}", config.getServletPath());
        } catch (ServletException | NamespaceException e) {
            LOGGER.error("Error while registering Jahia CSRF Guard JavaScriptServlet", e);
        }
    }
}
