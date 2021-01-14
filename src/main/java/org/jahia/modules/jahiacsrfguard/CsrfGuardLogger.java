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

import org.owasp.csrfguard.log.ILogger;
import org.owasp.csrfguard.log.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger implementation for CsrfGuard
 */
public class CsrfGuardLogger implements ILogger {
    private static final long serialVersionUID = 5581329713949000954L;
    private static final Logger logger = LoggerFactory.getLogger("Owasp.CsrfGuard");

    @Override
    public void log(String msg) {
        logger.info(msg);
    }

    @Override
    public void log(LogLevel level, String msg) {
        switch (level) {
            case Info:
                logger.info(msg);
                break;
            case Debug:
                logger.debug(msg);
                break;
            case Trace:
                logger.trace(msg);
                break;
            case Warning:
                logger.warn(msg);
                break;
            default:
                logger.error(msg);
                break;
        }
    }

    @Override
    public void log(Exception exception) {
        logger.error(exception.getLocalizedMessage(), exception);
    }

    @Override
    public void log(LogLevel level, Exception exception) {
        switch (level) {
            case Info:
                logger.info(exception.getLocalizedMessage(), exception);
                break;
            case Debug:
                logger.debug(exception.getLocalizedMessage(), exception);
                break;
            case Trace:
                logger.trace(exception.getLocalizedMessage(), exception);
                break;
            case Warning:
                logger.warn(exception.getLocalizedMessage(), exception);
                break;
            default:
                logger.error(exception.getLocalizedMessage(), exception);
                break;
        }
    }
}
