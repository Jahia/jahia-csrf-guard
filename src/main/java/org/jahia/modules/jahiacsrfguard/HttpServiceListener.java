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
package org.jahia.modules.jahiacsrfguard;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.osgi.framework.BundleContext;
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

    public void onBind(ServiceReference serviceReference) {
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

    public void onUnbind(ServiceReference serviceReference) {
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
