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
package org.jahia.modules.jahiacsrfguard.filters;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.bin.filters.CompositeFilter;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;
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
    private String tag;
    private String[] resolvedUrlPatterns;
    private boolean skipForUnauthentifiedUsers;

    @Override
    public void init(FilterConfig filterConfig) {
        this.tag = loadTag();
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

        // skip filter if connected user not match configured ones if not html content type of if path from the request or url resolver not match one of the provided patterns.
        if (!matchUser() || !matchHtmlContentType(responseWrapper) || !(matchPattern(requestPath) || matchUrlResolverPattern(httpRequest))) {
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

        Matcher closeHeadTagMatcher = CLOSE_HEAD_TAG_PATTERN.matcher(originalContent);
        if (closeHeadTagMatcher.find()) {
            logger.debug("Adding CSRFGuard JS to '{}'", httpRequest.getRequestURI());

            int indexOfCloseHeadTag = closeHeadTagMatcher.start();
            String codeSnippet = buildCodeSnippet(httpRequest.getContextPath());

            PrintWriter writer = response.getWriter();
            writer.write(originalContent.substring(0, indexOfCloseHeadTag));
            writer.write(codeSnippet);
            writer.write(originalContent.substring(indexOfCloseHeadTag));
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

    private boolean matchUser() {
        return !skipForUnauthentifiedUsers || !JahiaUserManagerService.isGuest(JCRSessionFactory.getInstance().getCurrentUser());
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public void setResolvedUrlPatterns(String[] resolvedUrlPatterns) {
        this.resolvedUrlPatterns = resolvedUrlPatterns;
    }

    public void setSkipForUnauthentifiedUsers(boolean skipForUnauthentifiedUsers) {
        this.skipForUnauthentifiedUsers = skipForUnauthentifiedUsers;
    }

    @SuppressWarnings("java:S3457")
    private String buildCodeSnippet(String contextPath) {
        String src = contextPath.concat(servletPath).concat("?").concat(tag);
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

    private String loadTag() {
        try (InputStream input = getClass().getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (input != null) {
                Properties properties = new Properties();
                properties.load(input);
                return DigestUtils.sha256Hex(properties.getProperty("Bundle-Version")).substring(0, 6);
            }
        } catch (Exception e) { //ignored
        }
        return "unknown";
    }
}
