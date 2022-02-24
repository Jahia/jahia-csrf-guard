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
