package org.jahia.modules.jahiacsrfguard;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.owasp.csrfguard.CsrfGuard;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.servlet.http.HttpSession;

public class CsrfGuardHttpSessionListener implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof JahiaContextLoaderListener.HttpSessionCreatedEvent) {
            // Same code than org.owasp.csrfguard.CsrfGuardHttpSessionListener
            HttpSession httpSession = ((JahiaContextLoaderListener.HttpSessionCreatedEvent) event).getSession();
            CsrfGuard csrfGuard = CsrfGuard.getInstance();
            csrfGuard.updateToken(httpSession);
        }
    }
}
