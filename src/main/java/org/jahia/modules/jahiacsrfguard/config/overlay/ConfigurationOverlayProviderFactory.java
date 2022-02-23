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
		properties.putAll(configurationOverlayProvider.propertiesOverrideMap());
		
		return new PropertiesConfigurationProvider(properties);
    }

}
