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

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.jahia.bin.listeners.HttpListener;
import org.owasp.csrfguard.CsrfGuard;
import org.owasp.csrfguard.session.ContainerSession;
import org.owasp.csrfguard.session.LogicalSession;

public class CsrfGuardHttpSessionListener implements HttpListener {

    @Override
    public void sessionCreated(final HttpSessionEvent event) {
        final HttpSession session = event.getSession();
        final LogicalSession logicalSession = new ContainerSession(session);
        CsrfGuard.getInstance().onSessionCreated(logicalSession);
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent event) {
        final HttpSession session = event.getSession();
        final LogicalSession logicalSession = new ContainerSession(session);
        CsrfGuard.getInstance().onSessionDestroyed(logicalSession);
    }

}
