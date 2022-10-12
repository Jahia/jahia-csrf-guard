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

import org.apache.commons.lang3.tuple.Pair;
import org.owasp.csrfguard.token.storage.Token;
import org.owasp.csrfguard.token.storage.TokenHolder;
import org.springframework.session.SessionRepository;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class SpringSessionTokenHolder implements TokenHolder {
    public static final String CSRF_TOKEN = "CSRF_TOKEN";
    private SessionRepository sessionRepository;

    public SpringSessionTokenHolder(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void setMasterToken(String sessionKey, String value) {
        Token token = getToken(sessionKey);
        if (token == null) {
            token = new SpringSessionToken(value);
        } else {
            token.setMasterToken(value);
        }
        saveToken(sessionKey, token);
    }

    @Override
    public String createMasterTokenIfAbsent(String sessionKey, Supplier<String> valueSupplier) {
        Token token = getToken(sessionKey);
        if (token == null) {
            token = new SpringSessionToken(valueSupplier.get());
            saveToken(sessionKey, token);
        }
        return token.getMasterToken();
    }

    @Override
    public String createPageTokenIfAbsent(String sessionKey, String resourceUri, Supplier<String> valueSupplier) {
        Token token = getToken(sessionKey);
        String pageToken;
        if (Objects.isNull(token)) {
            pageToken = valueSupplier.get();
            token = new SpringSessionToken(valueSupplier.get(), Pair.of(resourceUri, pageToken));
        } else {
            pageToken = token.setPageTokenIfAbsent(resourceUri, valueSupplier);
        }

        saveToken(sessionKey, token);
        return pageToken;
    }

    @Override
    public Token getToken(String sessionKey) {
        return sessionRepository.getSession(sessionKey).getAttribute(CSRF_TOKEN);
    }

    @Override
    public String getPageToken(String sessionKey, String resourceUri) {
        Token token = getToken(sessionKey);
        return Objects.nonNull(token) ? token.getPageToken(resourceUri) : null;
    }

    @Override
    public void setPageToken(String sessionKey, String resourceUri, String value) {
        Token token = getTokenOrException(sessionKey);
        token.setPageToken(resourceUri, value);
        saveToken(sessionKey, token);
    }

    @Override
    public void setPageTokens(String sessionKey, Map<String, String> pageTokens) {
        Token token = getTokenOrException(sessionKey);
        token.setPageTokens(pageTokens);
        saveToken(sessionKey, token);
    }

    @Override
    public Map<String, String> getPageTokens(String sessionKey) {
        return getTokenOrException(sessionKey).getPageTokens();
    }

    @Override
    public void remove(String sessionKey) {
        sessionRepository.getSession(sessionKey).removeAttribute(CSRF_TOKEN);
    }

    @Override
    public void rotateAllPageTokens(String sessionKey, Supplier<String> tokenValueSupplier) {
        final Token token = getTokenOrException(sessionKey);
        token.rotateAllPageTokens(tokenValueSupplier);
        saveToken(sessionKey, token);
    }

    @Override
    public void regenerateUsedPageToken(String sessionKey, String tokenFromRequest, Supplier<String> tokenValueSupplier) {
        final Token token = getTokenOrException(sessionKey);
        token.regenerateUsedPageToken(tokenFromRequest, tokenValueSupplier);
        saveToken(sessionKey, token);
    }

    private Token getTokenOrException(final String sessionKey) {
        Token token = getToken(sessionKey);

        if (Objects.isNull(token)) {
            throw new IllegalStateException("Token with the provided session key does not exist!");
        } else {
            return token;
        }
    }

    private void saveToken(final String sessionKey, Token token) {
        sessionRepository.getSession(sessionKey).setAttribute(CSRF_TOKEN, token);
    }
}
