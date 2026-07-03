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

    private static final int PAGE_SIZE = 10;

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

        int page = 1;
        String p = req.getParameter("page");
        if (p != null && !p.isEmpty()) {
            try {
                page = Integer.parseInt(p);
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {}
        }

        List<User> userList = userService.getAllUsers(page, PAGE_SIZE);
        int totalUsers = userService.countAllUsers();
        int totalPages = (int) Math.ceil((double) totalUsers / PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;

        req.setAttribute("userList", userList);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
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
