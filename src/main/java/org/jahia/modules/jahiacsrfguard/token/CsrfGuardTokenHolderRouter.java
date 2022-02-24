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
    private static CsrfGuardTokenHolderRouter instance;
    private static BundleContext bundleContext;
    public static CsrfGuardTokenHolderRouter getInstance() {
        return instance;
    }

    private static final ServiceListener tokenHolderServiceListener = event -> {
        if (instance == null) {
            return;
        }
        ServiceReference serviceReference = event.getServiceReference();
        switch(event.getType()) {
            case ServiceEvent.MODIFIED:
            case ServiceEvent.REGISTERED:
                instance.setTokenHolder((TokenHolder) bundleContext.getService(serviceReference));
                break;
            case ServiceEvent.UNREGISTERING:
                bundleContext.ungetService(serviceReference);
                instance.setTokenHolder(new InMemoryTokenHolder());
                break;
            default:
                break;
        }
    };

    public static void init(BundleContext newBundleContext) throws InvalidSyntaxException {
        bundleContext = newBundleContext;
        instance = new CsrfGuardTokenHolderRouter();
        // Use default implementation at initialization
        instance.setTokenHolder(new InMemoryTokenHolder());
        // Listen on TokenHolder from OSGI
        bundleContext.addServiceListener(tokenHolderServiceListener, "(objectClass=org.owasp.csrfguard.token.storage.TokenHolder)");
    }

    public static void destroy(BundleContext destroyedBundleContext) {
        // cleanup resources
        destroyedBundleContext.removeServiceListener(tokenHolderServiceListener);
        instance = null;
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
