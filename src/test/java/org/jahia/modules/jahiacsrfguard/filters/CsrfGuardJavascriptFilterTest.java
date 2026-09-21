package org.jahia.modules.jahiacsrfguard.filters;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CsrfGuardJavascriptFilterTest {

    private static HttpServletRequest request(String servletPath, String pathInfo, String requestUri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn(servletPath);
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getRequestURI()).thenReturn(requestUri);
        return request;
    }

    @Test
    public void theResolvedPathIsReadFromTheMappingAndNotFromTheRawUri() {
        // Each raw URI spells a path the container folds away, so a pattern would select the wrong resource.
        assertEquals("/cms/render/live/en/sites/x.html",
                resolved("/cms", "/render/live/en/sites/x.html", "//cms/render/live/en/sites/x.html"));
        assertEquals("/home.do", resolved("/home.do", null, "/home.do;jsessionid=ABC"));
        assertEquals("/cms/render/live/en/sites/x.html",
                resolved("/cms", "/render/live/en/sites/x.html", "/cms/./render/live/en/sites/x.html"));
    }

    @Test
    public void theResolvedPathDropsTheContextPath() {
        // The pattern is written against the path URLResolver answers, which carries no context path.
        assertEquals("/home.do", resolved("/home.do", null, "/jahia/home.do"));
    }

    @Test
    public void aRequestThatMapsNothingResolvesToTheEmptyPath() {
        assertEquals("", resolved(null, null, "/"));
    }

    private static String resolved(String servletPath, String pathInfo, String requestUri) {
        return CsrfGuardJavascriptFilter.resolvedPath(request(servletPath, pathInfo, requestUri));
    }
}
