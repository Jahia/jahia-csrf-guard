package org.jahia.modules.jahiacsrfguard.filters;

import org.apache.commons.lang3.StringUtils;
import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.modules.jahiacsrfguard.JahiaCsrfGuardConfig;
import org.jahia.modules.jahiacsrfguard.JahiaCsrfGuardConfigFactory;
import org.jahia.modules.jahiacsrfguard.JahiaCsrfGuardGlobalConfig;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * Applies the fetch metadata request policy: a request that writes content is served only when the browser reports
 * it as originating from the site's own browsing context, i.e. the <code>Sec-Fetch-Site</code> request header is
 * absent or holds anything other than <code>cross-site</code>.
 * <p>
 * A request writes content when its HTTP method is a write method, or when its query string supplies a
 * <code>jcrMethodToCall</code> method Jahia acts upon in place of the HTTP method. The query string is read on its
 * own so that the policy never triggers request body parsing, which the request encoding depends on.
 * <p>
 * The policy covers every URL except the ones it exempts itself, and both are configurable through the
 * {@code fetchMetadataUrlPatterns} and {@code fetchMetadataWhitelist} properties of any
 * {@code org.jahia.modules.jahiacsrfguard-*} configuration; {@code jahia.csrf-guard.fetchMetadata.enabled} drives the
 * policy as a whole.
 */
@Component(immediate = true, service = AbstractServletFilter.class)
public class FetchMetadataFilter extends AbstractServletFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchMetadataFilter.class);

    static final String SEC_FETCH_SITE_HEADER = "Sec-Fetch-Site";
    static final String CROSS_SITE = "cross-site";
    static final String JCR_METHOD_TO_CALL = "jcrMethodToCall";

    private static final Set<String> WRITE_METHODS = new HashSet<>(Arrays.asList("POST", "PUT", "DELETE", "PATCH"));

    /** URLs reached from another site by design: an identity provider posts its assertion back to Jahia on *.saml */
    private static final List<Pattern> BUILT_IN_WHITELIST = Collections.singletonList(JahiaCsrfGuardConfig.createUrlPattern("*.saml"));

    private final AtomicReference<JahiaCsrfGuardGlobalConfig> globalConfig = new AtomicReference<>();
    private final AtomicReference<Collection<JahiaCsrfGuardConfig>> configs = new AtomicReference<>(new HashSet<>());

    @Activate
    public void activate() {
        LOGGER.debug("Activating Jahia CSRF Guard Fetch Metadata Filter");
        setFilterName("Jahia CSRF Guard Fetch Metadata Filter");
        setMatchAllUrls(true);
        setUrlPatterns(new String[] { "/*" });
        // decide on the incoming request, before it is dispatched further and before the error page it may lead to
        setDispatcherTypes(Set.of(DispatcherType.REQUEST.name()));
        setOrder(-1.0f);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // nothing to initialize, the policy is driven by configuration
    }

    @Override
    public void destroy() {
        // nothing to release
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (isRejected(httpRequest)) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Rejected request (ip:{}, method:{}, uri:{}, {}:{})", httpRequest.getRemoteAddr(), httpRequest.getMethod(),
                        httpRequest.getRequestURI(), SEC_FETCH_SITE_HEADER, httpRequest.getHeader(SEC_FETCH_SITE_HEADER));
            }
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }

    boolean isRejected(HttpServletRequest request) {
        JahiaCsrfGuardGlobalConfig currentGlobalConfig = globalConfig.get();
        return currentGlobalConfig != null && currentGlobalConfig.isEnabled() && currentGlobalConfig.isFetchMetadataEnabled()
                && isCrossSite(request.getHeader(SEC_FETCH_SITE_HEADER))
                && isWriteRequest(request)
                && isFiltered(request) && !isWhiteListed(request);
    }

    /**
     * @param secFetchSite value of the {@code Sec-Fetch-Site} request header
     * @return true when the browser reports a cross-site initiator
     */
    static boolean isCrossSite(String secFetchSite) {
        return CROSS_SITE.equalsIgnoreCase(StringUtils.trimToEmpty(secFetchSite));
    }

    /**
     * @param request client request object for servlet
     * @return true when the request writes content, by its HTTP method or by the method its query string asks Jahia to call
     */
    static boolean isWriteRequest(HttpServletRequest request) {
        return isWriteMethod(request.getMethod()) || queryStringAsksForWrite(request.getQueryString());
    }

    /**
     * @param method a method name
     * @return true when the method writes content
     */
    static boolean isWriteMethod(String method) {
        return WRITE_METHODS.contains(StringUtils.upperCase(StringUtils.trimToEmpty(method)));
    }

    /**
     * Read the {@code jcrMethodToCall} values held by a query string. Names and values are decoded the way the
     * container decodes them, and every occurrence counts, so that a value reaching Jahia is a value seen here.
     *
     * @param queryString the raw query string, may be null
     * @return true when one of the values is a write method
     */
    static boolean queryStringAsksForWrite(String queryString) {
        if (StringUtils.isEmpty(queryString)) {
            return false;
        }
        for (String pair : StringUtils.split(queryString, '&')) {
            String name = decode(StringUtils.substringBefore(pair, "="));
            if (JCR_METHOD_TO_CALL.equals(name) && isWriteMethod(decode(StringUtils.substringAfter(pair, "=")))) {
                return true;
            }
        }
        return false;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            LOGGER.debug("Reading query string value as supplied: {}", value, e);
            return value;
        }
    }

    /**
     * Check all url pattern configurations to see whether the policy applies to the current request. The policy covers
     * every URL as long as no configuration sets a scope, so that it holds on an installation whose configuration files
     * predate it.
     */
    private boolean isFiltered(ServletRequest request) {
        Collection<JahiaCsrfGuardConfig> currentConfigs = configs.get();
        return currentConfigs.stream().noneMatch(JahiaCsrfGuardConfig::hasFetchMetadataUrlPatterns)
                || currentConfigs.stream().anyMatch(config -> config.isFetchMetadataFiltered(request));
    }

    /**
     * Check all whitelist configurations, and the URLs exempted by the module itself, to see whether the policy is
     * bypassed for the current request
     */
    private boolean isWhiteListed(ServletRequest request) {
        String uri = JahiaCsrfGuardConfig.normalizePath(((HttpServletRequest) request).getRequestURI());
        return BUILT_IN_WHITELIST.stream().anyMatch(pattern -> pattern.matcher(uri).matches())
                || configs.get().stream().anyMatch(config -> config.isFetchMetadataWhiteListed(request));
    }

    @Reference(service = JahiaCsrfGuardGlobalConfig.class, cardinality = ReferenceCardinality.MANDATORY, unbind = "-")
    public void setGlobalConfig(JahiaCsrfGuardGlobalConfig globalConfig) {
        this.globalConfig.set(globalConfig);
    }

    @Reference(service = JahiaCsrfGuardConfigFactory.class, policy = ReferencePolicy.DYNAMIC, bind = "setConfigs", unbind = "clearConfigs")
    public void setConfigs(JahiaCsrfGuardConfigFactory configFactory) {
        LOGGER.debug("Setting configurations from factory: {}", configFactory.getName());
        this.configs.set(configFactory.getConfigs());
    }

    public void clearConfigs(JahiaCsrfGuardConfigFactory configFactory) {
        LOGGER.debug("Clearing configurations from factory: {}", configFactory.getName());
        this.configs.set(new HashSet<>());
    }
}
