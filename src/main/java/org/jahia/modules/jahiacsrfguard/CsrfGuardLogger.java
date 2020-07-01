package org.jahia.modules.jahiacsrfguard;

import org.owasp.csrfguard.log.ILogger;
import org.owasp.csrfguard.log.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsrfGuardLogger implements ILogger {
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
