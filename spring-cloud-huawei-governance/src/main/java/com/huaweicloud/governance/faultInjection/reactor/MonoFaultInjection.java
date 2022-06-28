package com.huaweicloud.governance.faultInjection.reactor;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.FaultInjectionPolicy;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoOperator;

public class MonoFaultInjection<T> extends MonoOperator<T, T> {

    private final CorePublisherFaultInjectionOperator<T> operator;

    MonoFaultInjection(Mono<? extends T> source, GovernanceRequest governanceRequest, FaultInjectionPolicy faultInjectionPolicy) {
        super(source);
        this.operator = new CorePublisherFaultInjectionOperator<T>(source, governanceRequest, faultInjectionPolicy);
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        operator.subscribe(actual);
    }
}
