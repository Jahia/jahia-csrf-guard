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

import java.io.Serializable;
import java.time.LocalDateTime;

public final class SerializablePageTokenValue implements Serializable {

    private String pageTokenValue;
    private LocalDateTime localDateTime;

    private SerializablePageTokenValue(final String pageTokenValue) {
        this(pageTokenValue, LocalDateTime.now());
    }

    private SerializablePageTokenValue(final String pageTokenValue, final LocalDateTime localDateTime) {
        this.pageTokenValue = pageTokenValue;
        this.localDateTime = localDateTime;
    }

    public static SerializablePageTokenValue from(final String pageTokenValue) {
        return new SerializablePageTokenValue(pageTokenValue);
    }

    public static SerializablePageTokenValue from(final String pageTokenValue, final LocalDateTime localDateTime) {
        return new SerializablePageTokenValue(pageTokenValue, localDateTime);
    }

    public String getValue() {
        return this.pageTokenValue;
    }

    public LocalDateTime getCreationTime() {
        return this.localDateTime;
    }

    public void setPageTokenValue(String pageTokenValue) {
        this.pageTokenValue = pageTokenValue;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }
}
