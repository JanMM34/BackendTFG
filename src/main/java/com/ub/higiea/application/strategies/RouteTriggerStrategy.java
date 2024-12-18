package com.ub.higiea.application.strategies;

import com.ub.higiea.domain.model.Sensor;
import reactor.core.publisher.Mono;

public interface RouteTriggerStrategy {
    Mono<Boolean> shouldTriggerRoute(Sensor sensor);
}