/*
 * Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.junit.Test;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MultipartRequestWrapperTest {

    private static final String BOUNDARY = "----TestBoundary123";

    /**
     * Build a raw multipart/form-data body from a list of field name/value pairs.
     */
    private static byte[] buildMultipartBody(String boundary, String[]... fields) {
        StringBuilder sb = new StringBuilder();
        for (String[] field : fields) {
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"").append(field[0]).append("\"\r\n");
            sb.append("\r\n");
            sb.append(field[1]).append("\r\n");
        }
        sb.append("--").append(boundary).append("--\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Create a mock HttpServletRequest with the given multipart body.
     */
    private static HttpServletRequest mockMultipartRequest(String boundary, byte[] body) throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentType()).thenReturn("multipart/form-data; boundary=" + boundary);
        when(request.getContentLength()).thenReturn(body.length);
        when(request.getMethod()).thenReturn("POST");
        when(request.getCharacterEncoding()).thenReturn("UTF-8");

        ByteArrayInputStream bais = new ByteArrayInputStream(body);
        ServletInputStream sis = new ServletInputStream() {
            @Override public boolean isFinished() { return bais.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener readListener) { }
            @Override public int read() { return bais.read(); }
        };
        when(request.getInputStream()).thenReturn(sis);

        // No query-string parameters on the underlying request
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getParameterNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getParameter(anyString())).thenReturn(null);
        when(request.getParameterValues(anyString())).thenReturn(null);

        return request;
    }

    @Test
    public void testGetParameterReturnsCsrfToken() throws Exception {
        byte[] body = buildMultipartBody(BOUNDARY,
                new String[]{"OWASP-CSRFTOKEN", "abc-123-token"},
                new String[]{"username", "john"});

        HttpServletRequest mock = mockMultipartRequest(BOUNDARY, body);
        MultipartRequestWrapper wrapper = new MultipartRequestWrapper(mock);

        assertEquals("abc-123-token", wrapper.getParameter("OWASP-CSRFTOKEN"));
        assertEquals("john", wrapper.getParameter("username"));
    }

    @Test
    public void testGetParameterReturnsNullForMissingField() throws Exception {
        byte[] body = buildMultipartBody(BOUNDARY, new String[]{"field1", "value1"});
        HttpServletRequest mock = mockMultipartRequest(BOUNDARY, body);
        MultipartRequestWrapper wrapper = new MultipartRequestWrapper(mock);

        assertNull(wrapper.getParameter("nonexistent"));
    }

    @Test
    public void testGetParameterMapContainsAllFields() throws Exception {
        byte[] body = buildMultipartBody(BOUNDARY,
                new String[]{"token", "t1"},
                new String[]{"name", "Alice"});

        HttpServletRequest mock = mockMultipartRequest(BOUNDARY, body);
        MultipartRequestWrapper wrapper = new MultipartRequestWrapper(mock);

        Map<String, String[]> paramMap = wrapper.getParameterMap();
        assertEquals(2, paramMap.size());
        assertArrayEquals(new String[]{"t1"}, paramMap.get("token"));
        assertArrayEquals(new String[]{"Alice"}, paramMap.get("name"));
    }

    @Test
    public void testGetParameterNamesContainsAllFields() throws Exception {
        byte[] body = buildMultipartBody(BOUNDARY,
                new String[]{"alpha", "1"},
                new String[]{"beta", "2"});

        HttpServletRequest mock = mockMultipartRequest(BOUNDARY, body);
        MultipartRequestWrapper wrapper = new MultipartRequestWrapper(mock);

        Set<String> names = new HashSet<>();
        Enumeration<String> en = wrapper.getParameterNames();
        while (en.hasMoreElements()) {
            names.add(en.nextElement());
        }
        assertTrue(names.contains("alpha"));
        assertTrue(names.contains("beta"));
        assertEquals(2, names.size());
    }

    @Test
    public void testGetParameterValuesForMultiValueField() throws Exception {
        byte[] body = buildMultipartBody(BOUNDARY,
                new String[]{"color", "red"},
                new String[]{"color", "blue"});

        HttpServletRequest mock = mockMultipartRequest(BOUNDARY, body);
        MultipartRequestWrapper wrapper = new MultipartRequestWrapper(mock);

        String[] values = wrapper.getParameterValues("color");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("red", values[0]);
        assertEquals("blue", values[1]);
    }

    @Test
    public void testGetParameterValuesReturnsNullForMissingField() throws Exception {
        byte[] body = buildMultipartBody(BOUNDARY, new String[]{"a", "1"});
        HttpServletRequest mock = mockMultipartRequest(BOUNDARY, body);
        MultipartRequestWrapper wrapper = new MultipartRequestWrapper(mock);

        assertNull(wrapper.getParameterValues("missing"));
    }

    @Test
    public void testMultipartParametersMergeWithOriginalParams() throws Exception {
        byte[] body = buildMultipartBody(BOUNDARY, new String[]{"token", "csrf-value"});
        HttpServletRequest mock = mockMultipartRequest(BOUNDARY, body);

        // Simulate a query-string parameter on the underlying request
        Map<String, String[]> originalParams = new HashMap<>();
        originalParams.put("queryParam", new String[]{"qval"});
        when(mock.getParameterMap()).thenReturn(originalParams);
        when(mock.getParameter("queryParam")).thenReturn("qval");
        when(mock.getParameterValues("queryParam")).thenReturn(new String[]{"qval"});
        when(mock.getParameterNames()).thenReturn(Collections.enumeration(Collections.singleton("queryParam")));

        MultipartRequestWrapper wrapper = new MultipartRequestWrapper(mock);

        // Both parameters should be accessible
        assertEquals("csrf-value", wrapper.getParameter("token"));
        assertEquals("qval", wrapper.getParameter("queryParam"));

        Map<String, String[]> merged = wrapper.getParameterMap();
        assertEquals(2, merged.size());
        assertArrayEquals(new String[]{"csrf-value"}, merged.get("token"));
        assertArrayEquals(new String[]{"qval"}, merged.get("queryParam"));
    }

    @Test
    public void testParameterMapIsUnmodifiable() throws Exception {
        byte[] body = buildMultipartBody(BOUNDARY, new String[]{"f", "v"});
        HttpServletRequest mock = mockMultipartRequest(BOUNDARY, body);
        MultipartRequestWrapper wrapper = new MultipartRequestWrapper(mock);

        try {
            wrapper.getParameterMap().put("new", new String[]{"val"});
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // ok
        }
    }

    @Test
    public void testFileFieldsAreIgnored() throws Exception {
        // Build a body with a file part (has filename attribute) and a form field
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(BOUNDARY).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"test.txt\"\r\n");
        sb.append("Content-Type: text/plain\r\n");
        sb.append("\r\n");
        sb.append("file content here\r\n");
        sb.append("--").append(BOUNDARY).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"token\"\r\n");
        sb.append("\r\n");
        sb.append("csrf-val\r\n");
        sb.append("--").append(BOUNDARY).append("--\r\n");

        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        HttpServletRequest mock = mockMultipartRequest(BOUNDARY, body);
        MultipartRequestWrapper wrapper = new MultipartRequestWrapper(mock);

        // File field should not appear as parameter
        assertNull(wrapper.getParameter("file"));
        // Form field should be accessible
        assertEquals("csrf-val", wrapper.getParameter("token"));
        assertEquals(1, wrapper.getParameterMap().size());
    }

    @Test
    public void testWorksWithMultiReadHttpServletRequest() throws Exception {
        byte[] body = buildMultipartBody(BOUNDARY, new String[]{"csrf", "token-value"});
        HttpServletRequest mock = mockMultipartRequest(BOUNDARY, body);

        // Chain through MultiReadHttpServletRequest, as the production code does
        MultiReadHttpServletRequest multiRead = new MultiReadHttpServletRequest(mock);
        MultipartRequestWrapper wrapper = new MultipartRequestWrapper(multiRead);

        assertEquals("token-value", wrapper.getParameter("csrf"));

        // Verify the stream can still be read after parsing (the point of MultiReadHttpServletRequest)
        byte[] reRead = new byte[body.length];
        int bytesRead = multiRead.getInputStream().read(reRead);
        assertTrue("Stream should be re-readable after multipart parsing", bytesRead > 0);
    }
}
