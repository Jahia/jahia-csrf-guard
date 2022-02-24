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
package org.jahia.modules.jahiacsrfguard.service;

import javax.servlet.http.HttpSession;

/**
 * Service to handle session created and session destroyed
 * This will call the CSRF services to react on session lifecycle
 */
public interface CsrfGuardSessionHandler {
    /**
     * Used when the session have been created
     * @param session The newly created http session
     */
    void onSessionCreated(HttpSession session);

    /**
     * Used when the session have been destroyed or evicted
     * @param session The destroyed http session
     */
    void onSessionDestroyed(HttpSession session);
}
