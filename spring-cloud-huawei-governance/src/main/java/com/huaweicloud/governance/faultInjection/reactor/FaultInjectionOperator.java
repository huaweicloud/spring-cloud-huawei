package com.huaweicloud.governance.faultInjection.reactor;

import java.util.function.UnaryOperator;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.FaultInjectionPolicy;
import org.reactivestreams.Publisher;

import io.github.resilience4j.reactor.IllegalPublisherException;
import reactor.core.publisher.Mono;

public class FaultInjectionOperator<T> implements UnaryOperator<Publisher<T>> {

    private final GovernanceRequest governanceRequest;

    private final FaultInjectionPolicy faultInjectionPolicy;

    private FaultInjectionOperator(GovernanceRequest governanceRequest, FaultInjectionPolicy faultInjectionPolicy) {
        this.governanceRequest = governanceRequest;
        this.faultInjectionPolicy = faultInjectionPolicy;
    }

    public static <T> FaultInjectionOperator<T> of(
        GovernanceRequest governanceRequest,
        FaultInjectionPolicy faultInjectionPolicy) {
        return new FaultInjectionOperator<>(governanceRequest, faultInjectionPolicy);
    }

    @Override
    public Publisher<T> apply(Publisher<T> publisher) {
        if (publisher instanceof Mono) {
            return new MonoFaultInjection<>((Mono<? extends T>) publisher, governanceRequest, faultInjectionPolicy);
        } else {
            throw new IllegalPublisherException(publisher);
        }
    }
}
