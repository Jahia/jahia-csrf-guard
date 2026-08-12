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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Dynamic configuration to mainly set url patterns to apply CsrfGuardFilter on a request and whitelisting urls, which should be bypassed.
 */
public class JahiaCsrfGuardConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(JahiaCsrfGuardConfig.class);

    public static final String URL_PATTERNS = "urlPatterns";
    public static final String WHITELIST = "whitelist";
    public static final String CROSS_SITE_WRITE_URL_PATTERNS = "crossSiteWriteUrlPatterns";
    public static final String CROSS_SITE_WRITE_WHITELIST = "crossSiteWriteWhitelist";

    private String pid;
    private List<Pattern> urlPatterns;
    private List<Pattern> whitelistPatterns;
    private List<Pattern> crossSiteWriteUrlPatterns;
    private List<Pattern> crossSiteWriteWhitelistPatterns;

    public JahiaCsrfGuardConfig() {
    }

    public static JahiaCsrfGuardConfig build(String pid, Dictionary<String, ?> properties){
        LOGGER.debug("Building Jahia CSRF Guard configuration for pid: {}, config size: {}", pid, properties.size());
        JahiaCsrfGuardConfig config = new JahiaCsrfGuardConfig();
        config.pid = pid;
        String urlPatterns = (String) properties.get(URL_PATTERNS);
        if (StringUtils.isNotEmpty(urlPatterns)) {
            config.setUrlPatterns(urlPatterns);
        }
        String whitelist = (String) properties.get(WHITELIST);
        if (StringUtils.isNotEmpty(whitelist)) {
            config.setWhitelist(whitelist);
        }
        String crossSiteWriteUrlPatterns = (String) properties.get(CROSS_SITE_WRITE_URL_PATTERNS);
        if (StringUtils.isNotEmpty(crossSiteWriteUrlPatterns)) {
            config.setCrossSiteWriteUrlPatterns(crossSiteWriteUrlPatterns);
        }
        String crossSiteWriteWhitelist = (String) properties.get(CROSS_SITE_WRITE_WHITELIST);
        if (StringUtils.isNotEmpty(crossSiteWriteWhitelist)) {
            config.setCrossSiteWriteWhitelist(crossSiteWriteWhitelist);
        }
        return config;
    }

    public void setUrlPatterns(String urlPatterns) {
        this.urlPatterns = compile(urlPatterns);
    }

    public void setWhitelist(String whitelist) {
        this.whitelistPatterns = compile(whitelist);
    }

    public void setCrossSiteWriteUrlPatterns(String crossSiteWriteUrlPatterns) {
        this.crossSiteWriteUrlPatterns = compile(crossSiteWriteUrlPatterns);
    }

    public void setCrossSiteWriteWhitelist(String crossSiteWriteWhitelist) {
        this.crossSiteWriteWhitelistPatterns = compile(crossSiteWriteWhitelist);
    }

    private static List<Pattern> compile(String patterns) {
        return Arrays.stream(patterns.split(",")).map(String::trim).map(JahiaCsrfGuardConfig::createUrlPattern).collect(Collectors.toList());
    }

    /**
     * Sanitize and compile given regular expression pattern
     * @param pattern url patterns on which filter should be applied
     * @return compiled regular expression Pattern object
     */
    public static Pattern createUrlPattern(String pattern) {
        String patternToUse = pattern;
        if (!pattern.contains("*")) {
            patternToUse = pattern + (pattern.endsWith("/") ? "*" : "/*");
        }
        patternToUse = patternToUse.replace(".", "\\.");
        patternToUse = patternToUse.replaceAll("([^\\\\])\\*", "$1.*");
        patternToUse = patternToUse.replaceAll("^\\*", ".*");
        return Pattern.compile(patternToUse);
    }

    /**
     * Check url patterns configuration to see whether CsrfGuardFilter should be applied on current request
     * @param request client request object for servlet
     * @return true if CsrfGuardFilter should be applied
     */
    public boolean isFiltered(ServletRequest request) {
        return matches(urlPatterns, request);
    }

    /**
     * Check whitelist configuration to see whether CsrfGuardFilter should not be applied on current request
     * @param request client request object for servlet
     * @return true if URL is whitelisted for CsrfGuardFilter, so it should not be applied
     */
    public boolean isWhiteListed(ServletRequest request) {
        return matches(whitelistPatterns, request);
    }

    /**
     * @return true if this configuration sets the scope of the fetch metadata request policy
     */
    public boolean hasCrossSiteWriteUrlPatterns() {
        return crossSiteWriteUrlPatterns != null;
    }

    /**
     * Check url patterns configuration to see whether the fetch metadata request policy applies to the current request
     * @param request client request object for servlet
     * @return true if the policy should be applied
     */
    public boolean isCrossSiteWriteFiltered(ServletRequest request) {
        return matches(crossSiteWriteUrlPatterns, request);
    }

    /**
     * Check whitelist configuration to see whether the fetch metadata request policy is bypassed for the current request
     * @param request client request object for servlet
     * @return true if URL is whitelisted, so the policy should not be applied
     */
    public boolean isCrossSiteWriteWhiteListed(ServletRequest request) {
        return matches(crossSiteWriteWhitelistPatterns, request);
    }

    private static boolean matches(List<Pattern> patterns, ServletRequest request) {
        if (patterns == null) {
            return false;
        }
        String path = resolvedPath((HttpServletRequest) request);
        return patterns.stream().anyMatch(pattern -> pattern.matcher(path).matches());
    }

    /**
     * The path a pattern is compared against: the one the container resolved for this request, so that the spellings
     * Jahia serves as a single resource are matched as a single resource. Jahia's URLResolver drops a trailing slash
     * before the render pipeline reads a path, which is what makes {@code /home.action.do/} and {@code /home.action.do}
     * one URL to select, and the same holds for the other spellings the container folds away.
     * <p>
     * The servlet path and path info are what the container mapped: percent-decoded, with {@code /./} and repeated
     * slashes collapsed, {@code /../} resolved and path parameters parsed out. The context path is put back in front of
     * them, so a pattern keeps being written against the same URL it always was, and trailing slashes are dropped.
     * <p>
     * The matrix-parameter strip belongs to the raw URI, and applies only where the raw URI is what gets matched. On the
     * mapped path a {@code ;} is content the container decoded from a {@code %3B}, part of a segment's name — dropping
     * from it would cut the path short and take the suffix a pattern selects with it.
     * <p>
     * This is the path of the request being filtered. An internal forward, such as the one that serves a site URL
     * through {@code /cms/render/…}, is a separate dispatch this filter is not registered for and never reads.
     *
     * @param request client request object for servlet
     * @return the path the container resolved, never null
     */
    public static String resolvedPath(HttpServletRequest request) {
        String mapped = StringUtils.defaultString(request.getServletPath()) + StringUtils.defaultString(request.getPathInfo());
        if (StringUtils.isEmpty(mapped)) {
            // nothing mapped to read: match the raw URI, where a `;` still delimits path parameters
            return stripTrailingSlashes(normalizePath(request.getRequestURI()));
        }
        return stripTrailingSlashes(StringUtils.defaultString(request.getContextPath()) + mapped);
    }

    /**
     * @param path a request path
     * @return the path without the trailing slashes URLResolver drops, a bare {@code /} kept as is
     */
    static String stripTrailingSlashes(String path) {
        int end = path.length();
        while (end > 1 && path.charAt(end - 1) == '/') {
            end--;
        }
        return path.substring(0, end);
    }

    /**
     * Strip the matrix parameters from every segment of a raw request URI, the step {@link #resolvedPath} applies when a
     * raw URI is what it has to match.
     * Without this, a cross-site write to {@code /home.html;x=.saml} would end in {@code .saml}, match a {@code *.saml}
     * whitelist and bypass the policy, while Jahia's Render dispatch drops the matrix parameter and still writes
     * {@code /home.html}. The query string is not part of the URI, so it is untouched.
     *
     * @param uri a raw request URI, may be null
     * @return the URI with each segment's {@code ;matrix=params} removed
     */
    public static String normalizePath(String uri) {
        return uri == null ? "" : uri.replaceAll(";[^/]*", "");
    }

    @Override
    public String toString() {
        return "JahiaCsrfGuardConfig{" + "pid='" + pid + '\'' + '}';
    }
}
