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

import org.jahia.modules.jahiacsrfguard.service.CsrfGuardSessionHandler;
import org.owasp.csrfguard.CsrfGuard;
import org.owasp.csrfguard.session.ContainerSession;
import org.owasp.csrfguard.session.LogicalSession;

import javax.servlet.http.HttpSession;

public class CsrfGuardSessionHandlerImpl implements CsrfGuardSessionHandler {
    @Override
    public void onSessionCreated(HttpSession session) {
        final LogicalSession logicalSession = new ContainerSession(session);
        CsrfGuard.getInstance().onSessionCreated(logicalSession);
    }

    @Override
    public void onSessionDestroyed(HttpSession session) {
        final LogicalSession logicalSession = new ContainerSession(session);
        CsrfGuard.getInstance().onSessionDestroyed(logicalSession);
    }
}
