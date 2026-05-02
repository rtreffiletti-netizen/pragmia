package io.pragmia.kernel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "pragmia")
public class PragmiaProperties {

    private Instance instance = new Instance();
    private License  license  = new License();
    private Session  session  = new Session();
    private Admin    admin    = new Admin();

    @Data
    public static class Instance {
        private String name    = "PRAGMIA";
        private String baseUrl = "http://localhost:8080";
        private String version = "1.0.0";
    }

    @Data
    public static class License {
        private Type   type = Type.COMMUNITY;
        private String key  = "";

        public enum Type { COMMUNITY, ENTERPRISE }

        public boolean isEnterprise() { return type == Type.ENTERPRISE; }
    }

    @Data
    public static class Session {
        private Duration idleTimeout     = Duration.ofMinutes(30);
        private Duration absoluteTimeout = Duration.ofHours(8);
        private int      maxConcurrent   = 5;
    }

    @Data
    public static class Admin {
        private boolean  mtlsEnabled  = false;
        private Duration tokenExpiry  = Duration.ofMinutes(15);
    }
}
