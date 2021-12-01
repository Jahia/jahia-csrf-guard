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

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.bin.filters.CompositeFilter;
import org.jahia.services.render.URLResolver;
import org.owasp.csrfguard.CsrfGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
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
    private String[] resolvedUrlPatterns;

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
        // the original response output stream has been used, or the writer not used do nothing
        if (responseWrapper.isStreamUsed() || !responseWrapper.isWriterUsed()) {
            return;
        }
        String originalContent = responseWrapper.toString();
        int length = httpRequest.getContextPath().length();
        String requestPath = length > 0 ? httpRequest.getRequestURI().substring(length) : httpRequest.getRequestURI();

        // skip filter if not html content type of if path from the request or url resolver not match one of the provided patterns.
        if (!matchHtmlContentType(responseWrapper) || !(matchPattern(requestPath) || matchUrlResolverPattern(httpRequest))) {
            logger.debug("Not adding CSRFGuard JS to '{}'", httpRequest.getRequestURI());
            // In case of files, the response is already committed and cannot be overwritten.
            try {
                response.getWriter().write(originalContent);
            } catch (Exception e) {
                logger.warn("Response from {} has content that could not be written, set this class in debug for more details", httpRequest.getRequestURI());
                logger.debug("Response content is {}", originalContent, e);
            }
            return;
        }

        HttpSession httpSession = httpRequest.getSession(false);
        if (httpSession != null) {
            // Add a token to the session if there isn't one already
            CsrfGuard csrfGuard = CsrfGuard.getInstance();
//            csrfGuard.updateToken(httpSession);
        }

        Matcher closeHeadTagMatcher = CLOSE_HEAD_TAG_PATTERN.matcher(originalContent);
        if (closeHeadTagMatcher.find()) {
            logger.debug("Adding CSRFGuard JS to '{}'", httpRequest.getRequestURI());

            int indexOfCloseHeadTag = closeHeadTagMatcher.start();
            String codeSnippet = buildCodeSnippet(httpRequest.getContextPath());

            PrintWriter writer = response.getWriter();
            writer.write(originalContent.substring(0, indexOfCloseHeadTag));
            writer.write(codeSnippet);
            writer.write(originalContent.substring(indexOfCloseHeadTag));

            int contentLength = originalContent.length() + codeSnippet.length();
            response.setContentLength(contentLength);
        } else {
            response.getWriter().write(originalContent);
        }
    }

    private boolean matchHtmlContentType(HttpServletResponse response) {
        return StringUtils.contains(response.getContentType(), "text/html");
    }

    private boolean matchUrlResolverPattern(HttpServletRequest httpRequest) {
        URLResolver urlResolver = (URLResolver) httpRequest.getAttribute("urlResolver");
        return urlResolver != null  && matchPattern(urlResolver.getPath());
    }

    private boolean matchPattern(String requestPath) {
        for (String testPath : resolvedUrlPatterns) {
            if (CompositeFilter.matchFiltersURL(testPath, requestPath)) {
                return true;
            }
        }
        return false;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public void setResolvedUrlPatterns(String[] resolvedUrlPatterns) {
        this.resolvedUrlPatterns = resolvedUrlPatterns;
    }

    @SuppressWarnings("java:S3457")
    private String buildCodeSnippet(String contextPath) {
        String src = contextPath + servletPath;
        return String.format("<script type=\"text/javascript\" src=\"%s\"></script>\n", src);
    }

    private static final class ResponseWrapper extends HttpServletResponseWrapper {

        private final CharArrayWriter writer = new CharArrayWriter();
        private boolean streamUsed = false;
        private boolean writerUsed = false;

        ResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            writerUsed = true;
            return new PrintWriter(writer);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            streamUsed = true;
            return super.getOutputStream();
        }

        @Override
        public String toString() {
            return writer.toString();
        }

        public boolean isStreamUsed() {
            return streamUsed;
        }

        public boolean isWriterUsed() {
            return writerUsed;
        }
    }

}
