package kz.epam.campus.web;

import kz.epam.campus.model.User;
import kz.epam.campus.services.UserService;
import kz.epam.campus.services.security.SecurityService;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/users/*")
public class AdminUserServlet extends HttpServlet {

    private UserService userService;
    private SecurityService securityService;

    @Override
    public void init() throws ServletException {
        var ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        userService = ctx.getBean(UserService.class);
        securityService = ctx.getBean(SecurityService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (!securityService.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        List<User> userList = userService.getAllUsers();
        req.setAttribute("userList", userList);
        req.getRequestDispatcher("/WEB-INF/views/admin-users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        if (!securityService.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        int userId = Integer.parseInt(req.getParameter("userId"));
        userService.deactivateUser(userId);
        resp.sendRedirect("/admin/users");
    }
}
