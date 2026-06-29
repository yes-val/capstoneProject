<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="kz.epam.campus.model.User" %>
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
    <title><%= MessageHelper.msg(getServletContext(), locale, "admin.users.title") %></title>
</head>
<body>

<h1><%= MessageHelper.msg(getServletContext(), locale, "admin.users.heading") %></h1>

<p>
    <a href="<%= langPrefix %>lang=en">EN</a> |
    <a href="<%= langPrefix %>lang=ru">RU</a> |
    <a href="<%= langPrefix %>lang=kz">KZ</a>
</p>

<form method="POST" action="/logout" style="display:inline;">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "admin.equipment.logout") %></button>
</form>

<%
    List<User> userList = (List<User>) request.getAttribute("userList");
%>

<% if (userList == null || userList.isEmpty()) { %>
    <p><%= MessageHelper.msg(getServletContext(), locale, "admin.users.empty") %></p>
<% } else { %>
    <ul>
    <% for (User u : userList) { %>
        <li>
            <%= u.getName() %> (<%= u.getEmail() %>) —
            <%= MessageHelper.msg(getServletContext(), locale, "admin.users.role_label") %> <%= u.getPosition() %> —
            <%= MessageHelper.msg(getServletContext(), locale, "admin.users.status_label") %>
            <%= u.isActive()
                ? MessageHelper.msg(getServletContext(), locale, "admin.users.active")
                : MessageHelper.msg(getServletContext(), locale, "admin.users.inactive") %>

            <% if (u.isActive()) { %>
                <form method="POST" action="/admin/users" style="display:inline;">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <input type="hidden" name="userId" value="<%= u.getUserId() %>" />
                    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "admin.users.deactivate_button") %></button>
                </form>
            <% } %>
        </li>
    <% } %>
    </ul>
<% } %>

<p>
    <a href="/admin/equipment"><%= MessageHelper.msg(getServletContext(), locale, "admin.nav.equipment") %></a> |
    <a href="/admin/schedule"><%= MessageHelper.msg(getServletContext(), locale, "admin.nav.schedule") %></a> |
    <a href="/admin/users"><%= MessageHelper.msg(getServletContext(), locale, "admin.nav.users") %></a> |
    <a href="/profile"><%= MessageHelper.msg(getServletContext(), locale, "profile.heading") %></a>
</p>

</body>
</html>
