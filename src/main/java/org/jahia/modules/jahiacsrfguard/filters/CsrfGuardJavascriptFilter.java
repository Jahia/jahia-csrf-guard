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
 *     Copyright (C) 2002-2020 Jahia Solutions Group. All rights reserved.
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
package org.jahia.modules.jahiacsrfguard.filters;

import org.jahia.bin.filters.AbstractServletFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A servlet filter that adds CSRF guard Javascript to HTML pages.
 *
 * @author cmoitrier
 */
public final class CsrfGuardJavascriptFilter extends AbstractServletFilter {

    private static final Logger logger = LoggerFactory.getLogger(CsrfGuardJavascriptFilter.class);

    private static final Pattern CLOSE_HEAD_TAG_PATTERN = Pattern.compile("</head>", Pattern.CASE_INSENSITIVE);

    private String servletPath;

    @Override
    public void init(FilterConfig filterConfig) {
        // do nothing
    }

    @Override
    public void destroy() {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        ResponseWrapper responseWrapper = new ResponseWrapper(httpResponse);

        chain.doFilter(request, responseWrapper);

        String originalContent = responseWrapper.toString();
        Matcher closeHeadTagMatcher = CLOSE_HEAD_TAG_PATTERN.matcher(originalContent);
        if (closeHeadTagMatcher.find()) {
            logger.debug("Adding CSRFGuard JS to '{}'", httpRequest.getRequestURI());

            int indexOfCloseHeadTag = closeHeadTagMatcher.start();
            String codeSnippet = buildCodeSnippet(httpRequest.getContextPath());

            PrintWriter writer = response.getWriter();
            writer.write(originalContent.substring(0, indexOfCloseHeadTag - 1));
            writer.write(codeSnippet);
            writer.write(originalContent.substring(indexOfCloseHeadTag));

            int contentLength = originalContent.length() + codeSnippet.length();
            response.setContentLength(contentLength);
        }
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @SuppressWarnings("java:S3457")
    private String buildCodeSnippet(String contextPath) {
        String src = contextPath + servletPath;
        return String.format("<script type=\"text/javascript\" src=\"%s\"></script>\n", src);
    }

    private static final class ResponseWrapper extends HttpServletResponseWrapper {

        private final CharArrayWriter writer = new CharArrayWriter();

        ResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(writer);
        }

        @Override
        public String toString() {
            return writer.toString();
        }

    }

}
