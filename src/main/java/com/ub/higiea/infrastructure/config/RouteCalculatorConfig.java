package com.ub.higiea.infrastructure.config;

import com.azure.maps.route.MapsRouteAsyncClient;
import com.ub.higiea.application.strategies.RouteCalculator;
import com.ub.higiea.infrastructure.adapters.azuremaps.AzureMapsRouteCalculator;
import com.ub.higiea.application.strategies.implementations.MockRouteCalculatorImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteCalculatorConfig {

    @Bean
    @ConditionalOnProperty(name = "route.calculator.type", havingValue = "azure")
    public RouteCalculator azureRouteCalculator(MapsRouteAsyncClient mapsRouteAsyncClient) {
        return new AzureMapsRouteCalculator(mapsRouteAsyncClient);
    }

    @Bean
    @ConditionalOnMissingBean(RouteCalculator.class)
    public RouteCalculator mockRouteCalculator() {
        return new MockRouteCalculatorImpl();
    }

}
