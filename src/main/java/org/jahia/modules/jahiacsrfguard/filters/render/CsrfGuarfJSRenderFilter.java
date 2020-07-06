package org.jahia.modules.jahiacsrfguard.filters.render;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

/**
 * The CSRF Guard Filter add the csrfguard JS tag to the page,
 * this JS add the CSRF token to the required elements (e.g: form, ajax request, ...)
 */
public class CsrfGuarfJSRenderFilter extends AbstractFilter {

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String output = super.execute(previousOut, renderContext, resource, chain);
        String jsTagToAdd = "\n<jahia:resource type=\"javascript\" path=\""
                + renderContext.getURLGenerator().getContext() + "/JavaScriptServlet\"></jahia:resource>\n<";
        output += jsTagToAdd;
        return output;
    }
}
