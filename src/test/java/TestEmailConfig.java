import kz.epam.campus.services.EmailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Primary;

@Configuration
@Profile("test")
public class TestEmailConfig {

    @Bean
    @Primary
    public EmailService emailService() {
        // no-op in tests — don't attempt real SMTP connections
        return (toAddress, message) -> { };
    }
}
