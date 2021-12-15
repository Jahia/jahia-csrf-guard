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
package org.jahia.modules.jahiacsrfguard.config.overlay;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;


/**
 * Jahia CsrfGuard configuration overlay via OSGI configuration
 */
@Component(service = {ConfigurationOverlay.class, ManagedService.class}, property = "service.pid=org.owasp.csrfguard", immediate = true)
public class ConfigurationOverlay implements ManagedService {
    Map<String, String> overlayProperties = null;
    
    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        if (properties == null) {
            overlayProperties = null;
            return;
        }
        Map<String, String> csrfGuardProperties = new ConcurrentHashMap<>();
        Enumeration<String> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.startsWith("org.owasp.csrfguard")) {
                csrfGuardProperties.put(key, (String)properties.get(key));
            }
        }
        
        overlayProperties = csrfGuardProperties;
    }	
    
    public Map<String, String> getOverlayProperties() {
        return overlayProperties; 
    }
	
}
