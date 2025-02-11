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
package org.jahia.modules.jahiacsrfguard;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Dynamic configuration to mainly set url patterns to apply CsrfGuardFilter on a request and whitelisting urls, which should be bypassed.
 */
@Component(service = { JahiaCsrfGuardConfigFactory.class, ManagedServiceFactory.class}, immediate = true, property = "service.pid=org.jahia.modules.jahiacsrfguard")
public class JahiaCsrfGuardConfigFactory implements ManagedServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JahiaCsrfGuardConfigFactory.class);

    private final Map<String, JahiaCsrfGuardConfig> configs = new HashMap<>();

    public JahiaCsrfGuardConfigFactory() {
        LOGGER.debug("Creating Jahia CSRF Guard Config Factory");
    }

    @Override
    public String getName() {
        return "Jahia CSRF Guard Config Factory";
    }

    @Override
    public void updated(String pid, Dictionary<String, ?> properties) throws ConfigurationException {
        LOGGER.info("Updating Jahia CSRF Guard configuration for pid: {}, config size: {}", pid, properties.size());
        JahiaCsrfGuardConfig config = JahiaCsrfGuardConfig.build(pid, properties);
        configs.put(pid, config);
    }

    @Override
    public void deleted(String pid) {
        LOGGER.info("Deleting Jahia CSRF Guard configuration for pid: {}", pid);
        configs.remove(pid);
    }

    public Collection<JahiaCsrfGuardConfig> getConfigs() {
        return configs.values();
    }
}
