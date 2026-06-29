package kz.epam.campus.web;

import kz.epam.campus.config.MessageHelper;
import kz.epam.campus.model.User;
import kz.epam.campus.services.UserService;
import kz.epam.campus.services.security.CurrentUserService;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.Locale;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private UserService userService;
    private CurrentUserService currentUserService;

    @Override
    public void init() throws ServletException {
        var ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        userService = ctx.getBean(UserService.class);
        currentUserService = ctx.getBean(CurrentUserService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser();
        req.setAttribute("user", currentUser);
        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Locale locale = MessageHelper.resolveLocale(req);
        User currentUser = getCurrentUser();

        String currentPassword = req.getParameter("currentPassword");
        String newName = req.getParameter("name");
        String newEmail = req.getParameter("email");
        String newPassword = req.getParameter("newPassword");

        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            req.setAttribute("error", MessageHelper.msg(getServletContext(), locale, "profile.error.password_required"));
            req.setAttribute("user", currentUser);
            req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
            return;
        }

        boolean verified = userService.authenticate(currentUser.getEmail(), currentPassword).isPresent();

        if (!verified) {
            req.setAttribute("error", MessageHelper.msg(getServletContext(), locale, "profile.error.wrong_password"));
            req.setAttribute("user", currentUser);
            req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
            return;
        }

        if (newName != null && !newName.trim().isEmpty()) {
            currentUser.setName(newName.trim());
        }

        if (newEmail != null && !newEmail.trim().isEmpty()) {
            currentUser.setEmail(newEmail.trim());
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            currentUser.setPasswordHash(newPassword.trim());
        }

        userService.updateProfile(currentUser, newPassword != null && !newPassword.trim().isEmpty());

        req.setAttribute("success", MessageHelper.msg(getServletContext(), locale, "profile.success"));
        req.setAttribute("user", currentUser);
        req.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(req, resp);
    }

    private User getCurrentUser() {
        String email = currentUserService.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("No authenticated user"));
        return userService.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}
