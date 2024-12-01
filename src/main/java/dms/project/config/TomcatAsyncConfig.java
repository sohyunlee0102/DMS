package dms.project.config;

import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatAsyncConfig {

    @Bean
    public TomcatConnectorCustomizer asyncTimeoutCustomize() {
        return connector -> {
            connector.setAsyncTimeout(30000000); // 30분
        };
    }

}
