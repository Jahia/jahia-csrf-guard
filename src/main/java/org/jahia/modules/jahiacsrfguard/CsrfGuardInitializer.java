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

        ServletContext context = JahiaContextLoaderListener.getServletContext();

        context = (ServletContext) Proxy.newProxyInstance(CsrfGuardInitializer.class.getClassLoader(), new Class[]{ServletContext.class}, (Object proxy, Method method, Object[] args) -> {
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
