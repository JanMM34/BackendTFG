package com.ub.higiea.infrastructure.config;

import com.azure.maps.route.MapsRouteAsyncClient;
import com.ub.higiea.application.service.external.RouteCalculatorService;
import com.ub.higiea.domain.adapter.RouteCalculatorAdapter;
import com.ub.higiea.infrastructure.adapters.MockRouteCalculatorAdapter;
import com.ub.higiea.infrastructure.adapters.azuremaps.AzureMapsRouteCalculatorAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteCalculatorConfig {

    @Value("${route.calculator.type:mock}")
    private String calculatorType;

    private final MapsRouteAsyncClient mapsRouteAsyncClient;

    public RouteCalculatorConfig(MapsRouteAsyncClient mapsRouteAsyncClient) {
        this.mapsRouteAsyncClient = mapsRouteAsyncClient;
    }

    @Bean
    public RouteCalculatorAdapter routeCalculator() {
        switch (calculatorType) {
            case "azure":
                return new AzureMapsRouteCalculatorAdapter(mapsRouteAsyncClient);
            case "mock":
            default:
                return new MockRouteCalculatorAdapter();
        }
    }

}
