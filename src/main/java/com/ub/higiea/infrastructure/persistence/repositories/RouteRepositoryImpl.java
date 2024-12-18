package com.ub.higiea.infrastructure.persistence.repositories;

import com.ub.higiea.domain.model.Route;
import com.ub.higiea.domain.repository.RouteRepository;
import com.ub.higiea.infrastructure.persistence.entities.RouteEntity;
import com.ub.higiea.infrastructure.persistence.mapper.RouteMapper;
import com.ub.higiea.infrastructure.persistence.mapper.SensorMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class RouteRepositoryImpl implements RouteRepository {

    private final RouteEntityRepository routeEntityRepository;

    public RouteRepositoryImpl(RouteEntityRepository routeEntityRepository) {

        this.routeEntityRepository = routeEntityRepository;

    }

    @Override
    public Mono<Route> findById(String id) {
        return routeEntityRepository.findById(id)
                .map(RouteMapper::toDomain);
    }

    @Override
    public Flux<Route> findAll() {
        return routeEntityRepository.findAll()
                .map(RouteMapper::toDomain);
    }

    @Override
    public Mono<Route> save(Route route) {
        RouteEntity routeEntity = RouteMapper.toEntity(route);
        return routeEntityRepository.save(routeEntity)
                .map(RouteMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return routeEntityRepository.deleteById(id);
    }

}