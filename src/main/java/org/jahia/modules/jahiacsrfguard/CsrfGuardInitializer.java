package org.jahia.modules.jahiacsrfguard;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.owasp.csrfguard.CsrfGuardServletContextListener;

import javax.servlet.ServletContextEvent;

public class CsrfGuardInitializer {
    public void onStart() {
        CsrfGuardServletContextListener csrfGuardServletContextListener = new CsrfGuardServletContextListener();
        csrfGuardServletContextListener.contextInitialized(new ServletContextEvent(JahiaContextLoaderListener.getServletContext()));
    }
}
