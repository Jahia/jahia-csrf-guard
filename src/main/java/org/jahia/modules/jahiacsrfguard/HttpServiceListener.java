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
package org.jahia.modules.jahiacsrfguard;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.modules.jahiacsrfguard.token.CsrfGuardTokenHolderRouter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.owasp.csrfguard.CsrfGuardServletContextListener;
import org.owasp.csrfguard.servlet.JavaScriptServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

/**
 * Service listener to make sure that we don't block startup
 */
public class HttpServiceListener implements BundleContextAware {

    public static final Logger logger = LoggerFactory.getLogger(HttpServiceListener.class);

    JavaScriptServlet javaScriptServlet;
    BundleContext bundleContext;

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setJavaScriptServlet(JavaScriptServlet javaScriptServlet) {
        this.javaScriptServlet = javaScriptServlet;
    }

    public void onBind(@SuppressWarnings("java:S1172") ServiceReference serviceReference) throws InvalidSyntaxException {
        CsrfGuardTokenHolderRouter.init(bundleContext);
        registerServlet();
    }

    public void onUnbind(ServiceReference serviceReference) {
        unregisterServlet(serviceReference);
        CsrfGuardTokenHolderRouter.destroy(bundleContext);
    }

    private void registerServlet() {
        // The passed service reference is a proxy class that we cannot use to retrieve the real service object, so we simply look it up again
        ServiceReference realServiceReference = bundleContext.getServiceReference(HttpService.class.getName());
        HttpService httpService = (HttpService) bundleContext.getService(realServiceReference);
        try {
            httpService.registerServlet("/CsrfServlet", javaScriptServlet, null, null);
            logger.info("Successfully registered custom servlet at /modules/CsrfServlet");

            CsrfGuardServletContextListener csrfGuardServletContextListener = new CsrfGuardServletContextListener();
            csrfGuardServletContextListener.contextInitialized(new ServletContextEvent(JahiaContextLoaderListener.getServletContext()));
        } catch (ServletException | NamespaceException e) {
            logger.error("Error registering servlet", e);
        }
    }

    private void unregisterServlet(ServiceReference serviceReference) {
        if (serviceReference == null) {
            return;
        }
        ServiceReference realServiceReference = bundleContext.getServiceReference(HttpService.class.getName());
        if (realServiceReference == null) {
            return;
        }
        HttpService httpService = (HttpService) bundleContext.getService(realServiceReference);
        if (httpService == null) {
            return;
        }
        httpService.unregister("/CsrfServlet");
        logger.info("Successfully unregistered custom servlet from /modules/CsrfServlet");
    }
}
