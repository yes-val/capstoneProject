<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Locale" %>
<%@ page import="kz.epam.campus.model.User" %>
<%@ page import="kz.epam.campus.config.MessageHelper" %>
<%
    Locale locale = MessageHelper.resolveLocale(request);
    String langPrefix = MessageHelper.langQueryPrefix(request);
    User user = (User) request.getAttribute("user");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="/css/style.css" />
    <title><%= MessageHelper.msg(getServletContext(), locale, "profile.title") %></title>
</head>
<body>

<h1><%= MessageHelper.msg(getServletContext(), locale, "profile.heading") %></h1>

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

<% if (request.getAttribute("success") != null) { %>
    <p style="color: green;"><%= request.getAttribute("success") %></p>
<% } %>

<form method="POST" action="/profile">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

    <label for="name"><%= MessageHelper.msg(getServletContext(), locale, "profile.name_label") %></label>
    <input type="text" id="name" name="name" value="<%= user.getName() %>" />

    <label for="email"><%= MessageHelper.msg(getServletContext(), locale, "profile.email_label") %></label>
    <input type="email" id="email" name="email" value="<%= user.getEmail() %>" />

    <label for="newPassword"><%= MessageHelper.msg(getServletContext(), locale, "profile.new_password_label") %></label>
    <input type="password" id="newPassword" name="newPassword" />

    <label for="currentPassword"><%= MessageHelper.msg(getServletContext(), locale, "profile.current_password_label") %></label>
    <input type="password" id="currentPassword" name="currentPassword" required />

    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "profile.save_button") %></button>
</form>

<% if (user.getPosition().name().equals("ADMIN")) { %>
    <p><a href="/admin/equipment"><%= MessageHelper.msg(getServletContext(), locale, "admin.nav.equipment") %></a></p>
<% } else { %>
    <p><a href="/bookings"><%= MessageHelper.msg(getServletContext(), locale, "profile.back_link") %></a></p>
<% } %>

</body>
</html>
