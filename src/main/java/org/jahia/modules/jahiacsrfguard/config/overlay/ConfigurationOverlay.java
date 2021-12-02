package org.jahia.modules.jahiacsrfguard.config.overlay;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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
        Map<String, String> csrfGuardProperties = new HashMap<>();
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
