package org.jahia.modules.jahiacsrfguard.filters;

import net.htmlparser.jericho.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

import java.util.List;

public class CsrfGuarfJSFilter extends AbstractFilter {

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String output = super.execute(previousOut, renderContext, resource, chain);
        String jsTagToAdd = "\n<jahia:resource type=\"javascript\" path=\"" + renderContext.getURLGenerator().getContext() + "/JavaScriptServlet\"></jahia:resource>\n<";
        output = addJsTag(output, jsTagToAdd);
        return output;
    }

    private String addJsTag(String output, String jsTagToAdd) {
        Source source = new Source(output);
        OutputDocument outputDocument = new OutputDocument(source);
        List<Element> elementList = source.getAllElements(HTMLElementName.HEAD);
        if (elementList != null && !elementList.isEmpty()) {
            final EndTag bodyEndTag = elementList.get(0).getEndTag();
            outputDocument.replace(bodyEndTag.getBegin(), bodyEndTag.getBegin() + 1, jsTagToAdd);
        }
        output = outputDocument.toString().trim();
        return output;
    }
}
