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
package org.jahia.modules.jahiacsrfguard;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.owasp.csrfguard.servlet.JavaScriptServlet;

/**
 * Service tracker for the HttpService to register the {@link JavaScriptServlet}.
 */
@SuppressWarnings("java:S1149")
public class HttpServiceTracker extends ServiceTracker<HttpService, HttpService> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServiceTracker.class);

    /**
     * Constructor for OSGI ServiceTracker to add a HttpService
     * @param context bundle execution context
     */
    public HttpServiceTracker(BundleContext context) {
        super(context, HttpService.class.getName(), null);
    }

    @Override
    public HttpService addingService(ServiceReference<HttpService> reference) {
        HttpService httpService = super.addingService(reference);
        if (httpService == null) {
            return null;
        }

        try {
            Dictionary<String, Object> initParams = new Hashtable<>();
            initParams.put("inject-into-attributes", true);
            httpService.registerServlet("/CsrfServlet", new JavaScriptServlet(), initParams, null);
        } catch (Exception e) {
            logger.error("Cannot register CsrfServlet", e);
        }

        return httpService;
    }

    @Override
    public void removedService(ServiceReference<HttpService> reference, HttpService service) {
        service.unregister("/CsrfServlet");

        super.removedService(reference, service);
    }

}
