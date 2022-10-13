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
 *     Copyright (C) 2002-2020 Jahia Solutions Group. All rights reserved.
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
import org.owasp.csrfguard.token.storage.impl.PageTokenValue;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SerializableToken implements Token, Serializable {

    private String masterToken;
    private Map<String, SerializablePageTokenValue> pageTokens;

    public SerializableToken(final String masterToken) {
        this(masterToken, new ConcurrentHashMap<>());
    }

    public SerializableToken(final String masterToken, final Pair<String, String> pageToken) {
        this(masterToken, toMap(pageToken));
    }

    private SerializableToken(final String masterToken, final Map<String, SerializablePageTokenValue> pageTokens) {
        Objects.requireNonNull(masterToken, "Master token cannot be null");
        Objects.requireNonNull(pageTokens, "Page tokens cannot be null");

        this.masterToken = masterToken;
        this.pageTokens = new ConcurrentHashMap<>(pageTokens);
    }

    @Override
    public String getMasterToken() {
        return this.masterToken;
    }

    @Override
    public void setMasterToken(final String masterToken) {
        this.masterToken = masterToken;
    }

    @Override
    public String getPageToken(final String uri) {
        return this.pageTokens.get(uri).getValue();
    }

    @Override
    public PageTokenValue getTimedPageToken(final String uri) {
        return this.pageTokens.get(uri) != null ? PageTokenValue.from(this.pageTokens.get(uri).getValue(), this.pageTokens.get(uri).getCreationTime()) : null;
    }

    @Override
    public void setPageToken(final String uri, final String pageToken) {
        this.pageTokens.put(uri, SerializablePageTokenValue.from(pageToken));
    }

    @Override
    public String setPageTokenIfAbsent(final String uri, final Supplier<String> valueSupplier) {
        return this.pageTokens.computeIfAbsent(uri, k -> SerializablePageTokenValue.from(valueSupplier.get())).getValue();
    }

    @Override
    public Map<String, String> getPageTokens() {
        return this.pageTokens.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                e -> e.getValue().getValue()));
    }

    @Override
    public void setPageTokens(final Map<String, String> pageTokens) {
        this.pageTokens = pageTokens.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> SerializablePageTokenValue.from(e.getValue()),
                        (e1, e2) -> e2,
                        ConcurrentHashMap::new
                ));
    }

    @Override
    public void rotateAllPageTokens(final Supplier<String> tokenValueSupplier) {
        this.pageTokens.entrySet().forEach(e -> e.setValue(SerializablePageTokenValue.from(tokenValueSupplier.get())));
    }

    @Override
    public void regenerateUsedPageToken(final String tokenFromRequest, final Supplier<String> tokenValueSupplier) {
        this.pageTokens.replaceAll((k, v) -> v.getValue().equals(tokenFromRequest) ? SerializablePageTokenValue.from(tokenValueSupplier.get()) : v);
    }

    private static Map<String, SerializablePageTokenValue> toMap(final Pair<String, String> pageToken) {
        final Map<String, SerializablePageTokenValue> pageTokens = new ConcurrentHashMap<>();
        pageTokens.put(pageToken.getKey(), SerializablePageTokenValue.from(pageToken.getValue()));
        return pageTokens;
    }
}
