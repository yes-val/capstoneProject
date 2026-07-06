package kz.epam.campus.config;

import jakarta.servlet.DispatcherType;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import java.util.EnumSet;

public class SecurityWebAppInitializer extends AbstractSecurityWebApplicationInitializer {

    @Override
    protected EnumSet<DispatcherType> getSecurityDispatcherTypes() {
        // Spring Security 7 added FORWARD and INCLUDE to the defaults, which causes
        // internal servlet forwards (e.g. LoginServlet → login.jsp) to re-enter the
        // security filter and hit anyRequest().authenticated() — producing a redirect loop.
        // Exclude FORWARD and INCLUDE so only top-level requests are filtered.
        return EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR, DispatcherType.ASYNC);
    }
}
