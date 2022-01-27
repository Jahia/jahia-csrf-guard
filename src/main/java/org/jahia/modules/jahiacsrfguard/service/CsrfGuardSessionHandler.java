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
