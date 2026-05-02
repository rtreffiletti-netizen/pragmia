package io.pragmia.virgilio.mfa;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@ConditionalOnProperty(prefix = "pragmia.mfa", name = "enabled", havingValue = "true")
@ConfigurationProperties(prefix = "pragmia.mfa")
@ComponentScan(basePackages = "io.pragmia.virgilio.mfa")
@Configuration
public class MfaAutoConfiguration {
    private boolean totpEnabled = true;
    private boolean webauthnEnabled = true;
    private String rpId = "localhost";
    private String rpName = "PRAGMIA";
    private int totpDigits = 6;
    private String totpAlgorithm = "SHA1";
    public boolean isTotpEnabled() { return totpEnabled; }
    public void setTotpEnabled(boolean enabled) { this.totpEnabled = enabled; }
    public boolean isWebauthnEnabled() { return webauthnEnabled; }
    public void setWebauthnEnabled(boolean enabled) { this.webauthnEnabled = enabled; }
    public String getRpId() { return rpId; }
    public void setRpId(String id) { this.rpId = id; }
    public String getRpName() { return rpName; }
    public void setRpName(String name) { this.rpName = name; }
    public int getTotpDigits() { return totpDigits; }
    public void setTotpDigits(int digits) { this.totpDigits = digits; }
    public String getTotpAlgorithm() { return totpAlgorithm; }
    public void setTotpAlgorithm(String algo) { this.totpAlgorithm = algo; }
}
