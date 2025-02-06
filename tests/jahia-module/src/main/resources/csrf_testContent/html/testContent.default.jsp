<%@ page language=  "java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<c:url value="${url.base}${currentNode.path}.modifyTextAction.do" var="modifyTextActionURL"/>
<c:url value="${url.base}${currentNode.path}.logAction.do" var="logActionURL"/>
<c:url value="${url.base}${currentNode.path}.dummy.do" var="dummyActionURL"/>

<h3>${currentNode.properties['text'].string}</h3>

<script type="application/javascript">
    $.post('${logActionURL}', function (result) {
        console.log(result);
    }, 'json');

    var jahiaDummyCsrfTest = {
        csrfTestAction: function () {
            $.post('${modifyTextActionURL}', {newText: 'Hello Mars!'},
                function (result) {
                    console.log(result);
                    window.location.reload();
                }, 'json');
        }
    };
</script>

<a href="${dummyActionURL}" class="btn btn-primary">Call To Dummy Action (GET)</a>

<form method="POST" action="${modifyTextActionURL}">
    <input type="hidden" name="jcrNodeType" value="jnt:testContent">
    <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>">
    <input type="hidden" name="jcrResourceID" value="${currentNode.identifier}">
    <input type="hidden" name="newText" value="Hello Planet!">
    <button type="submit">Say Hello Planet</button>
</form>

<button onclick="jahiaDummyCsrfTest.csrfTestAction()" type="button">Say Hello Mars</button>
