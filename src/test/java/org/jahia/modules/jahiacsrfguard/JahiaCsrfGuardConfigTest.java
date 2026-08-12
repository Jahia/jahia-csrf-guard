package org.jahia.modules.jahiacsrfguard;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pins the path a pattern is matched against: the path Jahia resolves, not the raw request URI. All four matchers —
 * the token filter's {@code isFiltered}/{@code isWhiteListed} and the fetch metadata policy's two — route through the
 * same helper, so every case here holds for both. Two URLs Jahia serves as one must select the same patterns: a matrix
 * parameter does not hide a suffix, and a trailing slash does not hide one either.
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

    @Test
    public void urlPatternMatchingIgnoresATrailingSlash() {
        JahiaCsrfGuardConfig config = config(JahiaCsrfGuardConfig.URL_PATTERNS, "*.do");
        // Jahia serves these three spellings as one action, so the pattern that selects one selects them all
        assertTrue(config.isFiltered(mapped("/cms", "/render/default/en/sites/site/home.logAction.do")));
        assertTrue(config.isFiltered(mapped("/cms", "/render/default/en/sites/site/home.logAction.do/")));
        assertTrue(config.isFiltered(mapped("/cms", "/render/default/en/sites/site/home.logAction.do//")));
        assertFalse(config.isFiltered(mapped("/cms", "/render/default/en/sites/site/home.html")));
    }

    @Test
    public void whitelistMatchingCoversTheSameActionReachedWithATrailingSlash() {
        JahiaCsrfGuardConfig config = config(JahiaCsrfGuardConfig.WHITELIST, "*.logAction.do");
        assertTrue(config.isWhiteListed(mapped("", "/en/sites/site/home.logAction.do")));
        assertTrue(config.isWhiteListed(mapped("", "/en/sites/site/home.logAction.do/")));
        // the exemption stays attached to the action it names
        assertFalse(config.isWhiteListed(mapped("", "/en/sites/site/home.publish.do")));
        assertFalse(config.isWhiteListed(mapped("", "/en/sites/site/home.publish.do/")));
    }

    @Test
    public void resolvedPathReadsTheMappedPathAndFallsBackToTheRawUri() {
        // the servlet path and path info are what the container mapped: decoded, /./ and // collapsed
        assertEquals("/cms/render/en/home.publish.do", JahiaCsrfGuardConfig.resolvedPath(mapped("/cms", "/render/en/home.publish.do/")));
        // a request with no servlet mapping to read is matched on its raw URI, as before
        assertEquals("/en/home.publish.do", JahiaCsrfGuardConfig.resolvedPath(request("/en/home.publish.do/")));
        assertEquals("/en/home.publish.do", JahiaCsrfGuardConfig.resolvedPath(request("/en/home.publish.do;jsessionid=abc/")));
    }

    @Test
    public void stripTrailingSlashesKeepsARootPath() {
        assertEquals("/", JahiaCsrfGuardConfig.stripTrailingSlashes("/"));
        assertEquals("/", JahiaCsrfGuardConfig.stripTrailingSlashes("///"));
        assertEquals("/a", JahiaCsrfGuardConfig.stripTrailingSlashes("/a//"));
        assertEquals("/a/b", JahiaCsrfGuardConfig.stripTrailingSlashes("/a/b"));
        assertEquals("", JahiaCsrfGuardConfig.stripTrailingSlashes(""));
    }

    @Test
    public void shippedUrlPatternsSelectWhatTheirWordingSays() {
        assertTrue(JahiaCsrfGuardConfig.createUrlPattern("*.do").matcher("/en/sites/site/home.publish.do").matches());
        assertFalse(JahiaCsrfGuardConfig.createUrlPattern("*.do").matcher("/en/sites/site/home.html").matches());
        // the second shipped pattern ends in a backslash-escaped asterisk, so it selects a literal "/*" suffix
        Pattern literalStar = JahiaCsrfGuardConfig.createUrlPattern("*/\\*");
        assertTrue(literalStar.matcher("/en/sites/site/*").matches());
        assertFalse(literalStar.matcher("/en/sites/site/").matches());
    }

    private static JahiaCsrfGuardConfig config(String property, String value) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(property, value);
        return JahiaCsrfGuardConfig.build("org.jahia.modules.jahiacsrfguard-test", properties);
    }

    /** a request the container could not map to a servlet: only its raw URI can be read */
    private static HttpServletRequest request(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }

    /** a request as the container mapped it, the way a servlet reads it */
    private static HttpServletRequest mapped(String servletPath, String pathInfo) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn(servletPath);
        when(request.getPathInfo()).thenReturn(pathInfo);
        return request;
    }
}
