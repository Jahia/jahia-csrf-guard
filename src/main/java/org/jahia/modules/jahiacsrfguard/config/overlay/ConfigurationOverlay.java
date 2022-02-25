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
