package org.jahia.modules.jahiacsrfguard.filters.render;

import org.jahia.modules.jahiacsrfguard.Config;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

import java.util.List;
import java.util.Map;

/**
 * This filter will remove the {@link Config#OWASP_CSRFTOKEN} from the actionParameters attribute
 * to avoid an Exception when executing the flow.
 * The actionParameters attribute is set by {@link org.jahia.services.render.webflow.WebflowAction}
 * and is used in {@link org.jahia.services.render.webflow.WebflowDispatcherScript}
 */
public class WebflowActionRenderFilter extends AbstractFilter {
    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        Map<String, List<String>> parameters = (Map<String, List<String>>) renderContext.getRequest().getAttribute("actionParameters");
        if (parameters != null && parameters.containsKey(Config.OWASP_CSRFTOKEN)) {
            parameters.remove(Config.OWASP_CSRFTOKEN);
        }

        return super.prepare(renderContext, resource, chain);
    }
}
