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

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.owasp.csrfguard.token.storage.TokenHolder;
import org.owasp.csrfguard.token.storage.impl.InMemoryTokenHolder;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

/**
 * This class is a Singleton because the TokenHolder is instantiated by CSRF guard internally,
 * This router is able to detect and use a custom implementation from OSGI services or used a default implementation: InMemoryTokenHolder
 */
public class CsrfGuardTokenHolderRouter implements BundleContextAware {
    private static CsrfGuardTokenHolderRouter instance;

    private BundleContext bundleContext;
    private ServiceTracker st;

    // Singleton
    private CsrfGuardTokenHolderRouter() {
        instance = this;
    }

    public static CsrfGuardTokenHolderRouter getInstance() {
        return instance;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void init() {
        // Use default implementation at initialization
        setTokenHolder(new InMemoryTokenHolder());

        // Listen on TokenHolder from OSGI
        st = new ServiceTracker(bundleContext, "org.springframework.session.SessionRepository", null) {
            @Override
            public Object addingService(ServiceReference reference) {
                Object result = super.addingService(reference);
                setTokenHolder(new SpringSessionTokenHolder((SessionRepository) result));
                return result;
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                setTokenHolder(new InMemoryTokenHolder());
            }
        };
        st.open();
    }

    public void destroy() {
        st.close();
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
