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
    public void urlPatternMatchingOnARawUriDropsMatrixParameters() {
        JahiaCsrfGuardConfig config = config(JahiaCsrfGuardConfig.URL_PATTERNS, "*.do");
        // A matrix parameter does not hide the .do suffix the pattern selects. These requests carry no servlet mapping,
        // so they are matched on the raw URI, which is where a `;` delimits path parameters.
        assertTrue(config.isFiltered(request("/home.logAction.do;jsessionid=abc")));
        assertTrue(config.isFiltered(request("/home.logAction.do")));
    }

    @Test
    public void whitelistMatchingOnARawUriCannotBeForgedByAMatrixParameter() {
        JahiaCsrfGuardConfig config = config(JahiaCsrfGuardConfig.WHITELIST, "*.saml");
        // A request Jahia resolves to /home.html must not borrow the *.saml exemption through a matrix parameter
        assertFalse(config.isWhiteListed(request("/sites/site/home.html;x=.saml")));
        assertTrue(config.isWhiteListed(request("/sites/site/home.callback.saml")));
        assertTrue(config.isWhiteListed(request("/sites/site/home.callback.saml;jsessionid=abc")));
    }

    @Test
    public void urlPatternMatchingOnAMappedPathKeepsASemicolonInsideASegment() {
        JahiaCsrfGuardConfig config = config(JahiaCsrfGuardConfig.URL_PATTERNS, "*.do");
        // On a mapped path the container has already parsed path parameters out, so a `;` is a character the segment
        // name contains — it must not shorten the path and take the suffix the pattern selects with it.
        assertTrue(config.isFiltered(mapped("", "/en/sites/site/home;x.logAction.do")));
        assertTrue(config.isFiltered(mapped("", "/en/sites/site/home;x.logAction.do/")));
    }

    @Test
    public void matchingCarriesTheContextPath() {
        JahiaCsrfGuardConfig config = config(JahiaCsrfGuardConfig.URL_PATTERNS, "/jahia/cms/*");
        // A deployment under a context path keeps the URLs its patterns were written against
        assertTrue(config.isFiltered(mapped("/jahia", "/cms", "/render/default/en/sites/site/home.logAction.do")));
        assertFalse(config.isFiltered(mapped("", "/cms", "/render/default/en/sites/site/home.logAction.do")));
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
        // These mocks stand in for what the container hands a servlet. That it hands over a decoded path with /./ and
        // // already folded is a property of the container, asserted nowhere here — the javadoc on resolvedPath states
        // it, and the module's e2e suite is what exercises it.
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
    public void createUrlPatternReadsAnEscapedAsteriskAsALiteral() {
        assertTrue(JahiaCsrfGuardConfig.createUrlPattern("*.do").matcher("/en/sites/site/home.publish.do").matches());
        assertFalse(JahiaCsrfGuardConfig.createUrlPattern("*.do").matcher("/en/sites/site/home.html").matches());
        // A pattern whose asterisk is backslash-escaped selects that asterisk as a character: this compiles to a
        // literal "/*" suffix. The escape count a .cfg file has to carry for the value to arrive in this shape is a
        // separate question, tracked in #175 — this pins the compiler, not the configuration file.
        Pattern literalStar = JahiaCsrfGuardConfig.createUrlPattern("*/\\*");
        assertTrue(literalStar.matcher("/en/sites/site/*").matches());
        assertFalse(literalStar.matcher("/en/sites/site/x").matches());
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

    /** a request as the container mapped it, the way a servlet reads it, on a root-context deployment */
    private static HttpServletRequest mapped(String servletPath, String pathInfo) {
        return mapped("", servletPath, pathInfo);
    }

    /** a request as the container mapped it, under the given context path */
    private static HttpServletRequest mapped(String contextPath, String servletPath, String pathInfo) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn(contextPath);
        when(request.getServletPath()).thenReturn(servletPath);
        when(request.getPathInfo()).thenReturn(pathInfo);
        return request;
    }
}
