package org.jahia.modules.jahiacsrfguard.config.overlay;

import java.util.Properties;

import org.owasp.csrfguard.config.ConfigurationProvider;
import org.owasp.csrfguard.config.ConfigurationProviderFactory;
import org.owasp.csrfguard.config.PropertiesConfigurationProvider;

/**
 *
 */
public class ConfigurationOverlayProviderFactory implements
		ConfigurationProviderFactory {

	/**
	 * Default constructor
	 */
	public ConfigurationOverlayProviderFactory() {
	    // nothing needs to be done for now
	}

	/**
	 * @see org.owasp.csrfguard.config.ConfigurationProviderFactory#retrieveConfiguration(java.util.Properties)
	 */
	public ConfigurationProvider retrieveConfiguration(Properties originalProperties) {
		ConfigurationOverlayProvider configurationOverlayProvider = ConfigurationOverlayProvider.retrieveConfig();
		Properties properties = configurationOverlayProvider.properties();
		
		return new PropertiesConfigurationProvider(properties);
    }

}
