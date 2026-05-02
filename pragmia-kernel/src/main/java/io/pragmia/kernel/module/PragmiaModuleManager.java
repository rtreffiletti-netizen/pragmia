package io.pragmia.kernel.module;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "pragmia.modules")
public class PragmiaModuleManager {

    private boolean virgilioEnabled = true;
    private boolean cantoEnabled    = true;
    private boolean sogliaEnabled   = true;
    private boolean clioEnabled     = true;
    private boolean beatriceEnabled = false;
    private boolean minosEnabled    = false;
    private boolean luceEnabled     = false;
    private boolean sibillaEnabled  = false;

    public void logStatus() {
        log.info("  VIRGILIO:{} CANTO:{} SOGLIA:{} CLIO:{}",
            yn(virgilioEnabled), yn(cantoEnabled), yn(sogliaEnabled), yn(clioEnabled));
        log.info("  BEATRICE:{} MINOS:{} LUCE:{} SIBILLA:{}",
            yn(beatriceEnabled), yn(minosEnabled), yn(luceEnabled), yn(sibillaEnabled));
    }

    private String yn(boolean b) { return b ? "ON" : "off"; }
}
