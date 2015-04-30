<%@page session="false" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!doctype html>
<html>
    <head>
        <title>${fn:escapeXml(model.title)}</title>
    </head>
    <body>
        <h2>${fn:escapeXml(model.title)}</h2>
        <hr noshade>
        <ul>
            <c:forEach items="${model.files}" var="file">
                <li>
                    <a href="${fn:escapeXml(file.key)}">${fn:escapeXml(file.value)}</a>
                </li>
            </c:forEach>
        </ul>
        <hr noshade>
        <c:forEach items="${model.downloads}" var="download">
            <a href="${fn:escapeXml(download.key)}">${fn:escapeXml(download.value)}</a>  |  
        </c:forEach>  
        <em>${fn:escapeXml(model.serverLine)}</em>
    </body>
</html>