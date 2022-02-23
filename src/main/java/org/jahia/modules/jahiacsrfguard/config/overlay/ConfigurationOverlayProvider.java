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

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.jahia.osgi.BundleUtils;
import org.owasp.csrfguard.CsrfGuardServletContextListener;
import org.owasp.csrfguard.config.overlay.ConfigPropertiesCascadeBase;
import org.owasp.csrfguard.config.overlay.ConfigPropertiesCascadeCommonUtils;
import org.owasp.csrfguard.util.CsrfGuardUtils;


/**
 * Use configuration overlays that use the base properties as a default, and then decorate with an overlay file
 */
public class ConfigurationOverlayProvider extends ConfigPropertiesCascadeBase {
	/**
	 * 
	 */
	public static final String META_INF_CSRFGUARD_PROPERTIES = "META-INF/csrfguard.properties";

	/**
	 * base properties file
	 */
	public static final String OWASP_CSRF_GUARD_PROPERTIES = "Owasp.CsrfGuard.properties";

	/**
	 * ovrlay properties file
	 */
	public static final String OWASP_CSRF_GUARD_OVERLAY_PROPERTIES = "Owasp.CsrfGuard.overlay.properties";

	/**
	 * retrieve a config from the config file or from cache
	 * @return the config object
	 */
	public static ConfigurationOverlayProvider retrieveConfig() {
		return retrieveConfig(ConfigurationOverlayProvider.class);
	}

	/**
	 * Default constructor
	 */
	public ConfigurationOverlayProvider() {
	 // nothing needs to be done for now
	}

	/**
	 * @see org.owasp.csrfguard.config.overlay.ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey()
	 */
	@Override
	protected String getSecondsToCheckConfigKey() {
		return "org.owasp.csrfguard.configOverlay.secondsBetweenUpdateChecks";
	}

	/**
	 * @see org.owasp.csrfguard.config.overlay.ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
	 */
	@Override
	public void clearCachedCalculatedValues() {
	 // There are no calculated values in our configuration.
	}

	/**
	 * @see org.owasp.csrfguard.config.overlay.ConfigPropertiesCascadeBase#getMainConfigClasspath()
	 */
	@Override
	protected String getMainConfigClasspath() {
		return OWASP_CSRF_GUARD_OVERLAY_PROPERTIES;
	}

	/**
	 * @see org.owasp.csrfguard.config.overlay.ConfigPropertiesCascadeBase#getHierarchyConfigKey()
	 */
	@Override
	protected String getHierarchyConfigKey() {
		return "org.owasp.csrfguard.configOverlay.hierarchy" + (System.getProperty("java.vm.vendor").toLowerCase().contains("ibm") ? ".ibm" : "");
	}

	/**
	 * see which configs are available
	 */
	private static String mainExampleConfigClasspath = null;
	
	/**
	 * @see org.owasp.csrfguard.config.overlay.ConfigPropertiesCascadeBase#getMainExampleConfigClasspath()
	 */
	@Override
	protected String getMainExampleConfigClasspath() {

		//do not know the answer?
		if (mainExampleConfigClasspath == null) {

			//is the main config file there?
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(OWASP_CSRF_GUARD_PROPERTIES);
			if (inputStream != null) {
			    setMainExampleConfigClassPath(OWASP_CSRF_GUARD_PROPERTIES);
				CsrfGuardUtils.closeQuietly(inputStream);
			} else {
				inputStream = getClass().getClassLoader().getResourceAsStream(META_INF_CSRFGUARD_PROPERTIES);
				if (inputStream != null) {
				    setMainExampleConfigClassPath(META_INF_CSRFGUARD_PROPERTIES);
					CsrfGuardUtils.closeQuietly(inputStream);
				} else {
					//hmm, its not there, but use it anyways
				    setMainExampleConfigClassPath(OWASP_CSRF_GUARD_PROPERTIES);
				}
			}
			
		}
		
		//generally this is Owasp.CsrfGuard.properties
		return ConfigPropertiesCascadeCommonUtils.defaultIfBlank(CsrfGuardServletContextListener.getConfigFileName(), 
				mainExampleConfigClasspath);
	}
	
	private static void setMainExampleConfigClassPath(String configClassPath) {
	    mainExampleConfigClasspath = configClassPath;
	}

    @Override
    protected ConfigPropertiesCascadeBase retrieveFromConfigFiles() {
        ConfigPropertiesCascadeBase result = super.retrieveFromConfigFiles();
        ConfigurationOverlay osgiConfigOverlay = BundleUtils.getOsgiService(ConfigurationOverlay.class, null);
        if (osgiConfigOverlay != null) {
            Map<String, String> overlayProperties = osgiConfigOverlay.getOverlayProperties();
            result.propertiesOverrideMap().putAll(overlayProperties != null ? overlayProperties : Collections.emptyMap());
        }
        return result;
    }

    @Override
    protected boolean filesNeedReloadingBasedOnContents() {
        ConfigurationOverlay osgiConfigOverlay = BundleUtils.getOsgiService(ConfigurationOverlay.class, null);
        if (osgiConfigOverlay != null && osgiConfigOverlay.getOverlayProperties() != null && (propertiesOverrideMap() == null
                || !propertiesOverrideMap().equals(osgiConfigOverlay.getOverlayProperties()))) {
            return true;
        }
        return super.filesNeedReloadingBasedOnContents();
    }
    
    
}
