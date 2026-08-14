package org.jahia.modules.jahiacsrfguard.filters;

import org.jahia.modules.jahiacsrfguard.JahiaCsrfGuardConfig;
import org.jahia.modules.jahiacsrfguard.JahiaCsrfGuardConfigFactory;
import org.jahia.modules.jahiacsrfguard.JahiaCsrfGuardGlobalConfig;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FetchMetadataFilterTest {

    private static final String RENDER_URI = "/en/sites/mysite/home";

    private FetchMetadataFilter filter;

    @Before
    public void setUp() {
        filter = new FetchMetadataFilter();
        filter.setGlobalConfig(globalConfig(Collections.emptyMap()));
        filter.setConfigs(configFactory("/*", "*.saml"));
    }

    // --- Sec-Fetch-Site reading -------------------------------------------------------------------------------------

    @Test
    public void crossSiteIsRecognized() {
        assertTrue(FetchMetadataFilter.isCrossSite("cross-site"));
        assertTrue(FetchMetadataFilter.isCrossSite("Cross-Site"));
        assertTrue(FetchMetadataFilter.isCrossSite(" cross-site "));
    }

    @Test
    public void otherSiteValuesAreNotCrossSite() {
        assertFalse(FetchMetadataFilter.isCrossSite("same-origin"));
        assertFalse(FetchMetadataFilter.isCrossSite("same-site"));
        assertFalse(FetchMetadataFilter.isCrossSite("none"));
        assertFalse(FetchMetadataFilter.isCrossSite(""));
        assertFalse(FetchMetadataFilter.isCrossSite(null));
    }

    // --- write detection --------------------------------------------------------------------------------------------

    @Test
    public void writeMethodsAreRecognized() {
        assertTrue(FetchMetadataFilter.isWriteMethod("POST"));
        assertTrue(FetchMetadataFilter.isWriteMethod("PUT"));
        assertTrue(FetchMetadataFilter.isWriteMethod("DELETE"));
        assertTrue(FetchMetadataFilter.isWriteMethod("PATCH"));
        assertTrue(FetchMetadataFilter.isWriteMethod("put"));
        assertTrue(FetchMetadataFilter.isWriteMethod(" Put "));
    }

    @Test
    public void readMethodsAreNotWriteMethods() {
        assertFalse(FetchMetadataFilter.isWriteMethod("GET"));
        assertFalse(FetchMetadataFilter.isWriteMethod("HEAD"));
        assertFalse(FetchMetadataFilter.isWriteMethod("OPTIONS"));
        assertFalse(FetchMetadataFilter.isWriteMethod(""));
        assertFalse(FetchMetadataFilter.isWriteMethod(null));
    }

    @Test
    public void queryStringMethodIsRead() {
        assertTrue(FetchMetadataFilter.queryStringAsksForWrite("jcrMethodToCall=put"));
        assertTrue(FetchMetadataFilter.queryStringAsksForWrite("jcrMethodToCall=PUT"));
        assertTrue(FetchMetadataFilter.queryStringAsksForWrite("a=1&jcrMethodToCall=delete&b=2"));
        assertTrue(FetchMetadataFilter.queryStringAsksForWrite("jcrMethodToCall=get&jcrMethodToCall=put"));
    }

    @Test
    public void percentEncodedQueryStringMethodIsRead() {
        assertTrue(FetchMetadataFilter.queryStringAsksForWrite("jcrMethodToCall=%70ut"));
        assertTrue(FetchMetadataFilter.queryStringAsksForWrite("jcrMethodTo%43all=put"));
    }

    @Test
    public void queryStringWithoutWriteMethodIsRead() {
        assertFalse(FetchMetadataFilter.queryStringAsksForWrite("jcrMethodToCall=get"));
        assertFalse(FetchMetadataFilter.queryStringAsksForWrite("jcrMethodToCall="));
        assertFalse(FetchMetadataFilter.queryStringAsksForWrite("otherParam=put"));
        assertFalse(FetchMetadataFilter.queryStringAsksForWrite("jcrMethodToCallSuffix=put"));
        assertFalse(FetchMetadataFilter.queryStringAsksForWrite(""));
        assertFalse(FetchMetadataFilter.queryStringAsksForWrite(null));
    }

    @Test
    public void valuesThatDoNotDecodeAreReadAsSupplied() {
        assertTrue(FetchMetadataFilter.queryStringAsksForWrite("jcrMethodToCall=put&jcrMethodToCall=%zz"));
        assertFalse(FetchMetadataFilter.queryStringAsksForWrite("jcrMethodToCall=put%"));
        assertFalse(FetchMetadataFilter.queryStringAsksForWrite("jcrMethodToCall=%zz"));
    }

    // --- the policy -------------------------------------------------------------------------------------------------

    @Test
    public void crossSitePostIsRejected() {
        assertTrue(filter.isRejected(request("POST", RENDER_URI, null, "cross-site")));
    }

    @Test
    public void crossSiteGetSupplyingAWriteMethodIsRejected() {
        assertTrue(filter.isRejected(request("GET", RENDER_URI, "jcrMethodToCall=put&j:title=x", "cross-site")));
    }

    @Test
    public void crossSiteActionPostIsRejected() {
        assertTrue(filter.isRejected(request("POST", "/en/sites/site/home.logAction.do", null, "cross-site")));
    }

    @Test
    public void crossSiteGetIsServed() {
        assertFalse(filter.isRejected(request("GET", RENDER_URI, null, "cross-site")));
    }

    @Test
    public void sameSiteWriteIsServed() {
        assertFalse(filter.isRejected(request("POST", RENDER_URI, null, "same-site")));
        assertFalse(filter.isRejected(request("POST", RENDER_URI, null, "same-origin")));
        assertFalse(filter.isRejected(request("POST", RENDER_URI, null, "none")));
        assertFalse(filter.isRejected(request("PUT", RENDER_URI, null, null)));
    }

    @Test
    public void whitelistedUrlIsServed() {
        assertFalse(filter.isRejected(request("POST", "/sites/site/home.callback.saml", null, "cross-site")));
    }

    @Test
    public void matrixParameterCannotForgeAWhitelistedUrl() {
        // /home.html ends in .saml only through a matrix parameter Jahia's Render dispatch drops before writing home.html
        assertTrue(filter.isRejected(request("POST", "/sites/site/home.html;x=.saml", "jcrMethodToCall=put", "cross-site")));
    }

    @Test
    public void matrixParameterDoesNotHideAGenuinelyWhitelistedUrl() {
        assertFalse(filter.isRejected(request("POST", "/sites/site/home.callback.saml;jsessionid=abc", null, "cross-site")));
    }

    @Test
    public void urlOutOfScopeIsServed() {
        filter.setConfigs(configFactory("/cms/*", null));
        assertTrue(filter.isRejected(request("POST", "/cms/render/live/fr/home", null, "cross-site")));
        assertFalse(filter.isRejected(request("POST", RENDER_URI, null, "cross-site")));
    }

    @Test
    public void policyOffServesEverything() {
        filter.setGlobalConfig(globalConfig(Collections.singletonMap(JahiaCsrfGuardGlobalConfig.CROSS_SITE_WRITE_PROTECTION_ENABLED, "false")));
        assertFalse(filter.isRejected(request("POST", RENDER_URI, null, "cross-site")));
    }

    @Test
    public void moduleOffServesEverything() {
        filter.setGlobalConfig(globalConfig(Collections.singletonMap(JahiaCsrfGuardGlobalConfig.ENABLED, "false")));
        assertFalse(filter.isRejected(request("POST", RENDER_URI, null, "cross-site")));
    }

    @Test
    public void withoutConfigurationThePolicyCoversEveryUrl() {
        filter.clearConfigs(mock(JahiaCsrfGuardConfigFactory.class));
        assertTrue(filter.isRejected(request("POST", RENDER_URI, null, "cross-site")));
        assertTrue(filter.isRejected(request("POST", "/cms/render/live/fr/home", null, "cross-site")));
    }

    @Test
    public void withoutConfigurationTheSamlCallbackIsServed() {
        filter.clearConfigs(mock(JahiaCsrfGuardConfigFactory.class));
        assertFalse(filter.isRejected(request("POST", "/sites/site/home.callback.saml", null, "cross-site")));
    }

    // --- fixtures ---------------------------------------------------------------------------------------------------

    private static HttpServletRequest request(String method, String uri, String queryString, String secFetchSite) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getQueryString()).thenReturn(queryString);
        when(request.getHeader(FetchMetadataFilter.SEC_FETCH_SITE_HEADER)).thenReturn(secFetchSite);
        return request;
    }

    private static JahiaCsrfGuardGlobalConfig globalConfig(Map<String, String> properties) {
        JahiaCsrfGuardGlobalConfig config = new JahiaCsrfGuardGlobalConfig();
        config.modified(properties);
        return config;
    }

    private static JahiaCsrfGuardConfigFactory configFactory(String urlPatterns, String whitelist) {
        Dictionary<String, Object> properties = new Hashtable<>();
        if (urlPatterns != null) {
            properties.put(JahiaCsrfGuardConfig.CROSS_SITE_WRITE_URL_PATTERNS, urlPatterns);
        }
        if (whitelist != null) {
            properties.put(JahiaCsrfGuardConfig.CROSS_SITE_WRITE_WHITELIST, whitelist);
        }
        JahiaCsrfGuardConfigFactory factory = new JahiaCsrfGuardConfigFactory();
        try {
            factory.updated("org.jahia.modules.jahiacsrfguard-test", properties);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return factory;
    }
}
