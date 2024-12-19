package dms.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Configuration
public class SseConfig {

    @Bean
    public SseEmitter sseEmitter() {
        SseEmitter sseEmitter = new SseEmitter(3600000L);
        sseEmitter.onCompletion(() -> {
            sseEmitter.complete();
        });
        return sseEmitter;
    }

}
