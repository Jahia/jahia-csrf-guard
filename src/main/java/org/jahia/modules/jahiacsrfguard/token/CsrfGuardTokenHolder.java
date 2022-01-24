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
package org.jahia.modules.jahiacsrfguard.token;

import org.owasp.csrfguard.token.storage.Token;
import org.owasp.csrfguard.token.storage.TokenHolder;

import java.util.Map;
import java.util.function.Supplier;

public class CsrfGuardTokenHolder implements TokenHolder {
    @Override
    public void setMasterToken(String sessionKey, String value) {
        CsrfGuardTokenHolderRouter.getInstance().getTokenHolder().setMasterToken(sessionKey, value);
    }

    @Override
    public String createMasterTokenIfAbsent(String sessionKey, Supplier<String> valueSupplier) {
        return CsrfGuardTokenHolderRouter.getInstance().getTokenHolder().createMasterTokenIfAbsent(sessionKey, valueSupplier);
    }

    @Override
    public String createPageTokenIfAbsent(String sessionKey, String resourceUri, Supplier<String> valueSupplier) {
        return CsrfGuardTokenHolderRouter.getInstance().getTokenHolder().createPageTokenIfAbsent(sessionKey, resourceUri, valueSupplier);
    }

    @Override
    public Token getToken(String sessionKey) {
        return CsrfGuardTokenHolderRouter.getInstance().getTokenHolder().getToken(sessionKey);
    }

    @Override
    public String getPageToken(String sessionKey, String resourceUri) {
        return CsrfGuardTokenHolderRouter.getInstance().getTokenHolder().getPageToken(sessionKey, resourceUri);
    }

    @Override
    public void setPageToken(String sessionKey, String resourceUri, String value) {
        CsrfGuardTokenHolderRouter.getInstance().getTokenHolder().setPageToken(sessionKey, resourceUri, value);
    }

    @Override
    public void setPageTokens(String sessionKey, Map<String, String> pageTokens) {
        CsrfGuardTokenHolderRouter.getInstance().getTokenHolder().setPageTokens(sessionKey, pageTokens);
    }

    @Override
    public Map<String, String> getPageTokens(String sessionKey) {
        return CsrfGuardTokenHolderRouter.getInstance().getTokenHolder().getPageTokens(sessionKey);
    }

    @Override
    public void remove(String sessionKey) {
        CsrfGuardTokenHolderRouter.getInstance().getTokenHolder().remove(sessionKey);
    }

    @Override
    public void rotateAllPageTokens(String sessionKey, Supplier<String> tokenValueSupplier) {
        CsrfGuardTokenHolderRouter.getInstance().getTokenHolder().rotateAllPageTokens(sessionKey, tokenValueSupplier);
    }

    @Override
    public void regenerateUsedPageToken(String sessionKey, String tokenFromRequest, Supplier<String> tokenValueSupplier) {
        CsrfGuardTokenHolderRouter.getInstance().getTokenHolder().regenerateUsedPageToken(sessionKey, tokenFromRequest, tokenValueSupplier);
    }
}
