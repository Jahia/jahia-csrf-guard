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

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.owasp.csrfguard.CsrfGuardServletContextListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class CsrfGuardInitializer {
    public void onStart() {
        CsrfGuardServletContextListener csrfGuardServletContextListener = new CsrfGuardServletContextListener();

        ServletContext context = (ServletContext) Proxy.newProxyInstance(CsrfGuardInitializer.class.getClassLoader(),
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

        csrfGuardServletContextListener.contextInitialized(new ServletContextEvent(context));
    }
}
