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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.owasp.csrfguard.CsrfGuardServletContextListener;

/**
 * Activator for this OSGi bundle that creates an OSGI Http ServiceTracker to register the JavaScriptServlet
 * The class also applies configuration depending on JVM.
 */
public class Activator implements BundleActivator {
    
    private HttpServiceTracker httpTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        httpTracker = new HttpServiceTracker(context);
        httpTracker.open();
        
        CsrfGuardServletContextListener csrfGuardServletContextListener = new CsrfGuardServletContextListener();

        ServletContext servletContext = (ServletContext) Proxy.newProxyInstance(Activator.class.getClassLoader(),
                new Class[] { ServletContext.class }, (Object proxy, Method method, Object[] args) -> {
                    if (method.getName().equals("getInitParameter") && args[0].equals("Owasp.CsrfGuard.Config")) {
                        if (System.getProperty("java.vm.vendor").toLowerCase().contains("ibm")) {
                            return "META-INF/csrfguard-ibm.properties";
                        }
                        return "META-INF/csrfguard.properties";
                    } else {
                        return method.invoke(JahiaContextLoaderListener.getServletContext(), args);
                    }
                });

        csrfGuardServletContextListener.contextInitialized(new ServletContextEvent(servletContext));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        httpTracker.close();
    }

}
