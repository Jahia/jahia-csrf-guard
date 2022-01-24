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

import org.osgi.framework.*;
import org.owasp.csrfguard.token.storage.TokenHolder;
import org.owasp.csrfguard.token.storage.impl.InMemoryTokenHolder;

/**
 * This class is a Singleton because the TokenHolder is instantiated by CSRF guard internally,
 * This router is able to detect and use a custom implementation from OSGI services or used a default implementation: InMemoryTokenHolder
 */
public class CsrfGuardTokenHolderRouter {
    // Singleton
    private CsrfGuardTokenHolderRouter() {}
    private static CsrfGuardTokenHolderRouter INSTANCE;
    private static BundleContext bundleContext;
    public static CsrfGuardTokenHolderRouter getInstance() {
        return INSTANCE;
    }

    private static final ServiceListener tokenHolderServiceListener = event -> {
        if (INSTANCE == null) {
            return;
        }
        ServiceReference serviceReference = event.getServiceReference();
        switch(event.getType()) {
            case ServiceEvent.MODIFIED:
            case ServiceEvent.REGISTERED:
                INSTANCE.setTokenHolder((TokenHolder) bundleContext.getService(serviceReference));
                break;
            case ServiceEvent.UNREGISTERING:
                bundleContext.ungetService(serviceReference);
                INSTANCE.setTokenHolder(new InMemoryTokenHolder());
                break;
        }
    };

    public static void init(BundleContext newBundleContext) throws InvalidSyntaxException {
        bundleContext = newBundleContext;
        INSTANCE = new CsrfGuardTokenHolderRouter();
        // Use default implementation at initialization
        INSTANCE.setTokenHolder(new InMemoryTokenHolder());
        // Listen on TokenHolder from OSGI
        bundleContext.addServiceListener(tokenHolderServiceListener, "(objectClass=org.owasp.csrfguard.token.storage.TokenHolder)");
    }

    public static void destroy(BundleContext destroyedBundleContext) {
        // cleanup resources
        destroyedBundleContext.removeServiceListener(tokenHolderServiceListener);
        INSTANCE = null;
        bundleContext = null;
    }

    // instance data
    private TokenHolder tokenHolder;

    public TokenHolder getTokenHolder() {
        return tokenHolder;
    }

    private void setTokenHolder(TokenHolder tokenHolder) {
        this.tokenHolder = tokenHolder;
    }
}
