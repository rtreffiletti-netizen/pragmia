package io.pragmia.kernel;

import io.pragmia.kernel.config.PragmiaProperties;
import io.pragmia.kernel.module.PragmiaModuleManager;
import io.pragmia.kernel.node.NodeRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KernelStartupListener {

    private final PragmiaModuleManager modules;
    private final NodeRegistry nodeRegistry;
    private final PragmiaProperties props;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║  PRAGMIA v{}  |  {}  ║", props.getInstance().getVersion(), props.getLicense().getType());
        log.info("║  {}  ║", props.getInstance().getBaseUrl());
        log.info("╚══════════════════════════════════════════════╝");
        log.info("  Flow nodes registered: {}", nodeRegistry.getNodeCount());
        log.info("  Perché ogni accesso racconta una storia.");
        modules.logStatus();
    }
}
