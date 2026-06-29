<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.util.Locale" %>
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
    <title><%= MessageHelper.msg(getServletContext(), locale, "admin.schedule.title") %></title>
</head>
<body>

<h1><%= MessageHelper.msg(getServletContext(), locale, "admin.schedule.heading") %></h1>

<p>
    <a href="<%= langPrefix %>lang=en">EN</a> |
    <a href="<%= langPrefix %>lang=ru">RU</a> |
    <a href="<%= langPrefix %>lang=kz">KZ</a>
</p>

<form method="POST" action="/logout" style="display:inline;">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "admin.schedule.logout") %></button>
</form>

<%
    LocalDate selectedDate = (LocalDate) request.getAttribute("selectedDate");
    Boolean isWorkingDay = (Boolean) request.getAttribute("isWorkingDay");
%>

<form method="GET" action="/admin/schedule">
    <label for="date"><%= MessageHelper.msg(getServletContext(), locale, "admin.schedule.date_label") %></label>
    <input type="date" id="date" name="date" value="<%= selectedDate %>" />
    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "admin.schedule.view_button") %></button>
</form>

<h2><%= selectedDate %></h2>

<p>
    <%= MessageHelper.msg(getServletContext(), locale, "admin.schedule.status_label") %>
    <% if (isWorkingDay) { %>
        <strong style="color: green;"><%= MessageHelper.msg(getServletContext(), locale, "admin.schedule.working_day") %></strong>
    <% } else { %>
        <strong style="color: red;"><%= MessageHelper.msg(getServletContext(), locale, "admin.schedule.holiday") %></strong>
    <% } %>
</p>

<form method="POST" action="/admin/schedule/working-day" style="display:inline;">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
    <input type="hidden" name="date" value="<%= selectedDate %>" />
    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "admin.schedule.set_working_button") %></button>
</form>

<form method="POST" action="/admin/schedule/holiday" style="display:inline;">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
    <input type="hidden" name="date" value="<%= selectedDate %>" />
    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "admin.schedule.set_holiday_button") %></button>
</form>

<p>
    <a href="/admin/equipment"><%= MessageHelper.msg(getServletContext(), locale, "admin.nav.equipment") %></a> |
    <a href="/admin/schedule"><%= MessageHelper.msg(getServletContext(), locale, "admin.nav.schedule") %></a> |
    <a href="/admin/users"><%= MessageHelper.msg(getServletContext(), locale, "admin.nav.users") %></a> |
    <a href="/profile"><%= MessageHelper.msg(getServletContext(), locale, "profile.heading") %></a>
</p>

</body>
</html>
