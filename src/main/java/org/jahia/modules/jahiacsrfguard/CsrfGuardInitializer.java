package org.jahia.modules.jahiacsrfguard;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.owasp.csrfguard.CsrfGuardServletContextListener;

import javax.servlet.ServletContextEvent;

public class CsrfGuardInitializer {
    private CsrfGuardServletContextListener csrfGuardServletContextListener;

    public void onStart() {
        if (JahiaContextLoaderListener.isContextInitialized()) {
            csrfGuardServletContextListener.contextInitialized(new ServletContextEvent(JahiaContextLoaderListener.getServletContext()));
        }
    }

    public void setCsrfGuardServletContextListener(CsrfGuardServletContextListener csrfGuardServletContextListener) {
        this.csrfGuardServletContextListener = csrfGuardServletContextListener;
    }
}
