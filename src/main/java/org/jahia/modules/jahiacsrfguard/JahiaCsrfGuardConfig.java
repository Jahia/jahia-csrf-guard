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
 *
 */
public class JahiaCsrfGuardConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(JahiaCsrfGuardConfig.class);

    public static final String URL_PATTERNS = "urlPatterns";
    public static final String WHITELIST = "whitelist";

    private String pid;
    private List<Pattern> urlPatterns;
    private List<Pattern> whitelist;

    public JahiaCsrfGuardConfig() {
    }

    public static JahiaCsrfGuardConfig build(String pid, Dictionary<String, ?> properties){
        LOGGER.info("Building Jahia CSRF Guard configuration for pid: {}, config size: {}", pid, properties.size());
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
        return config;
    }

    public void setUrlPatterns(String urlPatterns) {
        this.urlPatterns = Arrays.stream(urlPatterns.split(",")).map(String::trim).map(JahiaCsrfGuardConfig::createUrlPattern).collect(Collectors.toList());
    }

    public void setWhitelist(String whitelist) {
        this.whitelist = Arrays.stream(whitelist.split(",")).map(String::trim).map(JahiaCsrfGuardConfig::createUrlPattern).collect(Collectors.toList());
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
        if (urlPatterns == null) {
            return false;
        }

        String uri = ((HttpServletRequest) request).getRequestURI();
        return urlPatterns.stream().anyMatch(pattern -> pattern.matcher(uri).matches());
    }

    /**
     * Check whitelist configuration to see whether CsrfGuardFilter should not be applied on current request
     * @param request client request object for servlet
     * @return true if URL is whitelisted for CsrfGuardFilter, so it should not be applied
     */
    public boolean isWhiteListed(ServletRequest request) {
        if (whitelist == null) {
            return false;
        }
        String uri = ((HttpServletRequest) request).getRequestURI();
        return whitelist.stream().anyMatch(pattern -> pattern.matcher(uri).matches());
    }

    @Override
    public String toString() {
        return "JahiaCsrfGuardConfig{" + "pid='" + pid + '\'' + '}';
    }
}
