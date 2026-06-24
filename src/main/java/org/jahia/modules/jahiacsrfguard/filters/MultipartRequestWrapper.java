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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.*;

/**
 * HttpServletRequest wrapper that parses multipart/form-data requests using Apache Commons FileUpload
 * and exposes form field parameters via the standard getParameter API.
 * This allows downstream filters (such as OWASP CsrfGuard) to read CSRF tokens
 * from multipart form submissions using getParameter().
 */
public class MultipartRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, List<String>> multipartParameters;

    public MultipartRequestWrapper(HttpServletRequest request) throws FileUploadException {
        super(request);
        this.multipartParameters = parseMultipartParameters(request);
    }

    private static Map<String, List<String>> parseMultipartParameters(HttpServletRequest request) throws FileUploadException {
        Map<String, List<String>> params = new LinkedHashMap<>();
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        List<FileItem> items = upload.parseRequest(request);
        for (FileItem item : items) {
            if (item.isFormField()) {
                params.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>()).add(item.getString());
            }
        }
        return params;
    }

    @Override
    public String getParameter(String name) {
        List<String> values = multipartParameters.get(name);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> originalParams = super.getParameterMap();
        Map<String, String[]> merged = new LinkedHashMap<>(originalParams);
        for (Map.Entry<String, List<String>> entry : multipartParameters.entrySet()) {
            if (!merged.containsKey(entry.getKey())) {
                merged.put(entry.getKey(), entry.getValue().toArray(new String[0]));
            }
        }
        return Collections.unmodifiableMap(merged);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Set<String> names = new LinkedHashSet<>();
        Enumeration<String> originalNames = super.getParameterNames();
        while (originalNames.hasMoreElements()) {
            names.add(originalNames.nextElement());
        }
        names.addAll(multipartParameters.keySet());
        return Collections.enumeration(names);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] original = super.getParameterValues(name);
        List<String> multipart = multipartParameters.get(name);
        if (original == null && multipart == null) {
            return null;
        }
        List<String> merged = new ArrayList<>();
        if (original != null) {
            merged.addAll(Arrays.asList(original));
        }
        if (multipart != null) {
            merged.addAll(multipart);
        }
        return merged.toArray(new String[0]);
    }
}
