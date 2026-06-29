<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="kz.epam.campus.model.Equipment" %>
<%@ page import="kz.epam.campus.config.MessageHelper" %>
<%
    Locale locale = MessageHelper.resolveLocale(request);
    String langPrefix = MessageHelper.langQueryPrefix(request);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="/css/style.css" />
    <title><%= MessageHelper.msg(getServletContext(), locale, "admin.equipment.title") %></title>
</head>
<body>

<h1><%= MessageHelper.msg(getServletContext(), locale, "admin.equipment.heading") %></h1>

<p>
    <a href="<%= langPrefix %>lang=en">EN</a> |
    <a href="<%= langPrefix %>lang=ru">RU</a> |
    <a href="<%= langPrefix %>lang=kz">KZ</a>
</p>

<form method="POST" action="/logout" style="display:inline;">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "admin.equipment.logout") %></button>
</form>

<% if (request.getAttribute("error") != null) { %>
    <p style="color: red;"><%= request.getAttribute("error") %></p>
<% } %>

<%
    List<Equipment> equipmentList = (List<Equipment>) request.getAttribute("equipmentList");
%>

<h2><%= MessageHelper.msg(getServletContext(), locale, "admin.equipment.active_heading") %></h2>

<% if (equipmentList == null || equipmentList.isEmpty()) { %>
    <p><%= MessageHelper.msg(getServletContext(), locale, "admin.equipment.empty") %></p>
<% } else { %>
    <ul>
    <% for (Equipment eq : equipmentList) { %>
        <li>
            <strong><%= eq.getName() %></strong> — <%= eq.getDescription() %>
            <form method="POST" action="/admin/equipment/deactivate" style="display:inline;">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                <input type="hidden" name="equipmentId" value="<%= eq.getEquipmentId() %>" />
                <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "admin.equipment.deactivate_button") %></button>
            </form>
        </li>
    <% } %>
    </ul>
<% } %>

<h2><%= MessageHelper.msg(getServletContext(), locale, "admin.equipment.add_heading") %></h2>

<form method="POST" action="/admin/equipment">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

    <label for="name"><%= MessageHelper.msg(getServletContext(), locale, "admin.equipment.name_label") %></label>
    <input type="text" id="name" name="name" required />

    <label for="description"><%= MessageHelper.msg(getServletContext(), locale, "admin.equipment.description_label") %></label>
    <input type="text" id="description" name="description" />

    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "admin.equipment.add_button") %></button>
</form>

<p>
    <a href="/admin/equipment"><%= MessageHelper.msg(getServletContext(), locale, "admin.nav.equipment") %></a> |
    <a href="/admin/schedule"><%= MessageHelper.msg(getServletContext(), locale, "admin.nav.schedule") %></a> |
    <a href="/admin/users"><%= MessageHelper.msg(getServletContext(), locale, "admin.nav.users") %></a> |
    <a href="/profile"><%= MessageHelper.msg(getServletContext(), locale, "profile.heading") %></a>
</p>

</body>
</html>
