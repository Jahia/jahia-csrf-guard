package org.jahia.modules.jahiacsrfguard;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pins the matrix-parameter normalization that {@link JahiaCsrfGuardConfig#matches} now applies. Because the existing
 * token filter's {@code isFiltered}/{@code isWhiteListed} route through the same helper, this normalization also shifts
 * their matching — a matrix-param URI is matched on the path Jahia resolves, not on the raw request URI.
 */
public class JahiaCsrfGuardConfigTest {

    @Test
    public void normalizePathStripsMatrixParametersPerSegment() {
        assertEquals("/home.html", JahiaCsrfGuardConfig.normalizePath("/home.html;x=.saml"));
        assertEquals("/a/b/c", JahiaCsrfGuardConfig.normalizePath("/a;p=1/b;q=2/c"));
        assertEquals("/home/", JahiaCsrfGuardConfig.normalizePath("/home/"));
        assertEquals("/a/", JahiaCsrfGuardConfig.normalizePath("/a;jsessionid=xyz/"));
        assertEquals("", JahiaCsrfGuardConfig.normalizePath(null));
    }

    @Test
    public void urlPatternMatchingNormalizesMatrixParameters() {
        JahiaCsrfGuardConfig config = config(JahiaCsrfGuardConfig.URL_PATTERNS, "*.do");
        // a matrix parameter no longer hides the .do suffix the pattern selects
        assertTrue(config.isFiltered(request("/home.logAction.do;jsessionid=abc")));
        assertTrue(config.isFiltered(request("/home.logAction.do")));
    }

    @Test
    public void whitelistMatchingCannotBeForgedByAMatrixParameter() {
        JahiaCsrfGuardConfig config = config(JahiaCsrfGuardConfig.WHITELIST, "*.saml");
        // a request Jahia resolves to /home.html must not borrow the *.saml exemption through a matrix parameter
        assertFalse(config.isWhiteListed(request("/sites/site/home.html;x=.saml")));
        assertTrue(config.isWhiteListed(request("/sites/site/home.callback.saml")));
        assertTrue(config.isWhiteListed(request("/sites/site/home.callback.saml;jsessionid=abc")));
    }

    private static JahiaCsrfGuardConfig config(String property, String value) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(property, value);
        return JahiaCsrfGuardConfig.build("org.jahia.modules.jahiacsrfguard-test", properties);
    }

    private static HttpServletRequest request(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }
}
