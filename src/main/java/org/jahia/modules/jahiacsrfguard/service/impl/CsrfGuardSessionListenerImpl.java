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
package org.jahia.modules.jahiacsrfguard.service.impl;

import javax.servlet.http.HttpSessionEvent;

import org.jahia.bin.listeners.HttpListener;
import org.jahia.modules.jahiacsrfguard.service.CsrfGuardSessionHandler;
import org.jahia.modules.jahiacsrfguard.service.CsrfGuardSessionListener;

public class CsrfGuardSessionListenerImpl implements HttpListener, CsrfGuardSessionListener {
    private CsrfGuardSessionHandler csrfGuardSessionHandler;
    private boolean enabled = true;

    @Override
    public void sessionCreated(final HttpSessionEvent event) {
        if (enabled) {
            csrfGuardSessionHandler.onSessionCreated(event.getSession());
        }
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent event) {
        if (enabled) {
            csrfGuardSessionHandler.onSessionDestroyed(event.getSession());
        }
    }

    public void setCsrfGuardSessionHandler(CsrfGuardSessionHandler csrfGuardSessionHandler) {
        this.csrfGuardSessionHandler = csrfGuardSessionHandler;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
