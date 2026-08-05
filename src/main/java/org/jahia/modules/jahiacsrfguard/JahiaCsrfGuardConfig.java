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
    public static final String FETCH_METADATA_URL_PATTERNS = "fetchMetadataUrlPatterns";
    public static final String FETCH_METADATA_WHITELIST = "fetchMetadataWhitelist";

    private String pid;
    private List<Pattern> urlPatterns;
    private List<Pattern> whitelistPatterns;
    private List<Pattern> fetchMetadataUrlPatterns;
    private List<Pattern> fetchMetadataWhitelistPatterns;

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
        String fetchMetadataUrlPatterns = (String) properties.get(FETCH_METADATA_URL_PATTERNS);
        if (StringUtils.isNotEmpty(fetchMetadataUrlPatterns)) {
            config.setFetchMetadataUrlPatterns(fetchMetadataUrlPatterns);
        }
        String fetchMetadataWhitelist = (String) properties.get(FETCH_METADATA_WHITELIST);
        if (StringUtils.isNotEmpty(fetchMetadataWhitelist)) {
            config.setFetchMetadataWhitelist(fetchMetadataWhitelist);
        }
        return config;
    }

    public void setUrlPatterns(String urlPatterns) {
        this.urlPatterns = compile(urlPatterns);
    }

    public void setWhitelist(String whitelist) {
        this.whitelistPatterns = compile(whitelist);
    }

    public void setFetchMetadataUrlPatterns(String fetchMetadataUrlPatterns) {
        this.fetchMetadataUrlPatterns = compile(fetchMetadataUrlPatterns);
    }

    public void setFetchMetadataWhitelist(String fetchMetadataWhitelist) {
        this.fetchMetadataWhitelistPatterns = compile(fetchMetadataWhitelist);
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
    public boolean hasFetchMetadataUrlPatterns() {
        return fetchMetadataUrlPatterns != null;
    }

    /**
     * Check url patterns configuration to see whether the fetch metadata request policy applies to the current request
     * @param request client request object for servlet
     * @return true if the policy should be applied
     */
    public boolean isFetchMetadataFiltered(ServletRequest request) {
        return matches(fetchMetadataUrlPatterns, request);
    }

    /**
     * Check whitelist configuration to see whether the fetch metadata request policy is bypassed for the current request
     * @param request client request object for servlet
     * @return true if URL is whitelisted, so the policy should not be applied
     */
    public boolean isFetchMetadataWhiteListed(ServletRequest request) {
        return matches(fetchMetadataWhitelistPatterns, request);
    }

    private static boolean matches(List<Pattern> patterns, ServletRequest request) {
        if (patterns == null) {
            return false;
        }
        String uri = normalizePath(((HttpServletRequest) request).getRequestURI());
        return patterns.stream().anyMatch(pattern -> pattern.matcher(uri).matches());
    }

    /**
     * Strip the matrix parameters from every segment of a request URI, so a pattern is matched against the path Jahia
     * resolves rather than the raw URI. Without this, a cross-site write to {@code /home.html;x=.saml} would end in
     * {@code .saml}, match a {@code *.saml} whitelist and bypass the policy, while Jahia's Render dispatch drops the
     * matrix parameter and still writes {@code /home.html}. The query string is not part of the URI, so it is untouched.
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
