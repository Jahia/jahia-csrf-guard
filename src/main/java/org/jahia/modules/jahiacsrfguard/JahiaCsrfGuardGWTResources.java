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
 *     Copyright (C) 2002-2025 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.modules.jahiacsrfguard;

import org.jahia.ajax.gwt.helper.ModuleGWTResources;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.List;

/**
 * @author Jerome Blanchard
 */
@Component(service = ModuleGWTResources.class, immediate = true)
public class JahiaCsrfGuardGWTResources extends ModuleGWTResources {

    @Reference(service = JahiaCsrfGuardGlobalConfig.class, policy = ReferencePolicy.DYNAMIC, updated = "setConfig")
    private void setConfig(JahiaCsrfGuardGlobalConfig config) {
        if (config != null) {
            super.setJavascriptResources(List.of(config.getServletPath()));
        }
    }

    private void unsetConfig(JahiaCsrfGuardGlobalConfig config) {
    }

}
