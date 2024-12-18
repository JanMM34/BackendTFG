package com.ub.higiea.application.strategies.implementations;

import com.ub.higiea.application.strategies.RouteTriggerStrategy;
import com.ub.higiea.domain.model.ContainerState;
import com.ub.higiea.domain.model.Sensor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class FullContainerRouteTrigger implements RouteTriggerStrategy{

    @Override
    public Mono<Boolean> shouldTriggerRoute(Sensor sensor) {
        return Mono.just(sensor.getContainerState() == ContainerState.FULL);
    }

}
