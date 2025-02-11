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

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jahia.osgi.BundleUtils;
import org.owasp.csrfguard.CsrfGuardServletContextListener;
import org.owasp.csrfguard.config.overlay.ConfigPropertiesCascadeBase;
import org.owasp.csrfguard.config.overlay.ConfigPropertiesCascadeCommonUtils;

/**
 * Use configuration overlays that use the base properties as a default, and then decorate with an overlay file
 */
public class ConfigurationOverlayProvider extends ConfigPropertiesCascadeBase {

	public static final String META_INF_CSRFGUARD_PROPERTIES = "META-INF/csrfguard.properties";

	/**
	 * base properties file
	 */
	public static final String OWASP_CSRF_GUARD_PROPERTIES = "Owasp.CsrfGuard.properties";

	/**
	 * overlay properties file
	 */
	public static final String OWASP_CSRF_GUARD_OVERLAY_PROPERTIES = "Owasp.CsrfGuard.overlay.properties";

	/**
	 * retrieve a config from the config file or from cache
	 * @return the config object
	 */
	public static ConfigurationOverlayProvider retrieveConfig() {
		return retrieveConfig(ConfigurationOverlayProvider.class);
	}

	public ConfigurationOverlayProvider() {
	}

	/**
	 * @see org.owasp.csrfguard.config.overlay.ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey()
	 */
	@Override
	protected String getSecondsToCheckConfigKey() {
		return "org.owasp.csrfguard.configOverlay.secondsBetweenUpdateChecks";
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
			    ConfigPropertiesCascadeCommonUtils.closeQuietly(inputStream);
			} else {
				inputStream = getClass().getClassLoader().getResourceAsStream(META_INF_CSRFGUARD_PROPERTIES);
				if (inputStream != null) {
				    setMainExampleConfigClassPath(META_INF_CSRFGUARD_PROPERTIES);
				    ConfigPropertiesCascadeCommonUtils.closeQuietly(inputStream);
				} else {
					//hmm, its not there, but use it anyways
				    setMainExampleConfigClassPath(OWASP_CSRF_GUARD_PROPERTIES);
				}
			}

		}

		//generally this is Owasp.CsrfGuard.properties
		return StringUtils.defaultIfBlank(CsrfGuardServletContextListener.getConfigFileName(), mainExampleConfigClasspath);
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
