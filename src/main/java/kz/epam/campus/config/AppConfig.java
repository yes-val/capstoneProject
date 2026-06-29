package kz.epam.campus.config;

import kz.epam.campus.services.EmailService;
import kz.epam.campus.services.security.CurrentUserService;
import kz.epam.campus.services.security.PasswordEncoderService;
import kz.epam.campus.services.security.SecurityService;
import kz.epam.campus.services.security.impl.CurrentUserServiceImpl;
import kz.epam.campus.services.security.impl.PasswordEncoderServiceImpl;
import kz.epam.campus.services.security.impl.SecurityServiceImpl;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

@Configuration
@ComponentScan("kz.epam.campus")
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        ConnectionPool pool = new ConnectionPool(
                "jdbc:h2:mem:lab;DB_CLOSE_DELAY=-1",
                "sa",
                "",
                2,
                10,
                20000
        );

        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("schema.sql");
             Connection c = pool.getConnection();
             Statement st = c.createStatement()) {
            String sql = new String(in.readAllBytes());
            for (String s : sql.split(";")) {
                if (!s.trim().isEmpty()) {
                    st.execute(s);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize schema", e);
        }

        return pool;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource ms =
                new ReloadableResourceBundleMessageSource();

        ms.setBasename("classpath:messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);

        return ms;
    }

    @Bean
    public PasswordEncoderService passwordEncoderService(PasswordEncoder encoder) {
        return new PasswordEncoderServiceImpl(encoder);
    }

    @Bean
    public CurrentUserService currentUserService() {
        return new CurrentUserServiceImpl();
    }

    @Bean
    public SecurityService securityService() {
        return new SecurityServiceImpl();
    }

    @Bean
    public EmailService emailService() {
        return EmailConfigLoader.load();
    }
}
