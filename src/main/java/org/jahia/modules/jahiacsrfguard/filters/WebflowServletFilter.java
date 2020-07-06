package org.jahia.modules.jahiacsrfguard.filters;

import org.jahia.bin.filters.AbstractServletFilter;
import org.jahia.modules.jahiacsrfguard.Config;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.*;

/**
 * This filter will catch all the request that contains both tokens:
 * ${@link Config#OWASP_CSRFTOKEN} and ${@link WebflowServletFilter#WEBFLOW_TOKEN}
 * So it can remove the ${@link Config#OWASP_CSRFTOKEN} from the request
 */
public class WebflowServletFilter extends AbstractServletFilter {

    private static final String WEBFLOW_TOKEN = "webflowToken";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to do
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request.getParameterMap().containsKey(WEBFLOW_TOKEN) && request.getParameterMap().containsKey(Config.OWASP_CSRFTOKEN)) {
            HttpServletRequest req = new WebflowServletRequestWrapper((HttpServletRequest) request, new HashMap<>(request.getParameterMap()));
            chain.doFilter(req, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

    private static class WebflowServletRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String[]> parameterMap;

        public WebflowServletRequestWrapper(HttpServletRequest request, Map<String, String[]> parameterMap) {
            super(request);
            parameterMap.remove(Config.OWASP_CSRFTOKEN);
            this.parameterMap = parameterMap;
        }

        @Override
        public String getParameter(String name) {
            return (parameterMap.get(name) != null && parameterMap.get(name).length > 0) ? parameterMap.get(name)[0] : null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return parameterMap;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return new Vector<>(parameterMap.keySet()).elements();
        }

        @Override
        public String[] getParameterValues(String name) {
            return parameterMap.get(name);
        }
    }
}

