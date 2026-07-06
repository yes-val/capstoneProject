import kz.epam.campus.config.AppConfig;
import kz.epam.campus.config.SecurityBeansConfig;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

public abstract class BaseDbTest {

    protected static AnnotationConfigApplicationContext ctx;
    protected static DataSource dataSource;

    @BeforeAll
    static void init() throws Exception {
        ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().setActiveProfiles("test");
        ctx.register(AppConfig.class, SecurityBeansConfig.class, TestEmailConfig.class);
        ctx.refresh();

        dataSource = ctx.getBean(DataSource.class);

        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement()) {

            String sql = new String(
                    BaseDbTest.class.getClassLoader()
                            .getResourceAsStream("schema.sql")
                            .readAllBytes()
            );

            for (String s : sql.split(";")) {
                if (!s.trim().isEmpty()) {
                    st.execute(s);
                }
            }
        }
    }
}