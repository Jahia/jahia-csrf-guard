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
