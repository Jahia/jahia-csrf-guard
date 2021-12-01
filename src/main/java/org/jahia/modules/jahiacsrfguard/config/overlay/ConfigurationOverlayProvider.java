package org.jahia.modules.jahiacsrfguard.config.overlay;

import java.io.InputStream;

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
	 * 
	 */
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
	 * @see org.owasp.csrfguard.config.overlay.ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
	 */
	@Override
	public void clearCachedCalculatedValues() {
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
				mainExampleConfigClasspath = OWASP_CSRF_GUARD_PROPERTIES;
				CsrfGuardUtils.closeQuietly(inputStream);
			} else {
				inputStream = getClass().getClassLoader().getResourceAsStream(META_INF_CSRFGUARD_PROPERTIES);
				if (inputStream != null) {
					mainExampleConfigClasspath = META_INF_CSRFGUARD_PROPERTIES;
					CsrfGuardUtils.closeQuietly(inputStream);
				} else {
					//hmm, its not there, but use it anyways
					mainExampleConfigClasspath = OWASP_CSRF_GUARD_PROPERTIES;
				}
			}
			
		}
		
		//generally this is Owasp.CsrfGuard.properties
		return ConfigPropertiesCascadeCommonUtils.defaultIfBlank(CsrfGuardServletContextListener.getConfigFileName(), 
				mainExampleConfigClasspath);
	}

    @Override
    protected ConfigPropertiesCascadeBase retrieveFromConfigFiles() {
        ConfigPropertiesCascadeBase result = super.retrieveFromConfigFiles();
        ConfigurationOverlay osgiConfigOVerlay = BundleUtils.getOsgiService(ConfigurationOverlay.class, null);
        if (osgiConfigOVerlay != null) {
            result.propertiesOverrideMap().putAll(osgiConfigOVerlay.getOverlayProperties());
        }
        return result;
    }

    @Override
    protected boolean filesNeedReloadingBasedOnContents() {
        ConfigurationOverlay osgiConfigOVerlay = BundleUtils.getOsgiService(ConfigurationOverlay.class, null);
        if (osgiConfigOVerlay != null && osgiConfigOVerlay.getOverlayProperties() != null && (propertiesOverrideMap() == null
                || !osgiConfigOVerlay.getOverlayProperties().equals(propertiesOverrideMap()))) {
            return true;
        }
        return super.filesNeedReloadingBasedOnContents();
    }
    
    
}
