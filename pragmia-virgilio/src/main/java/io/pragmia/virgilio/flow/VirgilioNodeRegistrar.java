package io.pragmia.virgilio.flow;

import io.pragmia.kernel.node.NodeRegistry;
import io.pragmia.virgilio.flow.nodes.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(10)
public class VirgilioNodeRegistrar implements ApplicationRunner {

    private final NodeRegistry nodeRegistry;
    private final UsernamePasswordNode upNode;
    private final TotpMfaNode          totpNode;
    private final ConditionNode        condNode;
    private final AllowNode            allowNode;
    private final DenyNode             denyNode;

    @Override
    public void run(ApplicationArguments args) {
        nodeRegistry.register(allowNode);
        nodeRegistry.register(denyNode);
        nodeRegistry.register(upNode);
        nodeRegistry.register(totpNode);
        nodeRegistry.register(condNode);
        log.info("[Virgilio] 5 flow nodes registered");
    }
}
