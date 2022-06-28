package com.huaweicloud.governance.faultInjection.reactor;

import java.time.Duration;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.FaultInjectionPolicy;

import com.huaweicloud.governance.faultInjection.FaultExecutor;
import com.huaweicloud.governance.faultInjection.FaultInjectionException;

import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

public class CorePublisherFaultInjectionOperator<T> {

    private final CorePublisher<? extends T> source;
    private final GovernanceRequest governanceRequest;
    private final FaultInjectionPolicy faultInjectionPolicy;
    private boolean executed = false;

    CorePublisherFaultInjectionOperator(CorePublisher<? extends T> source, GovernanceRequest governanceRequest, FaultInjectionPolicy faultInjectionPolicy) {
        this.source = source;
        this.governanceRequest = governanceRequest;
        this.faultInjectionPolicy = faultInjectionPolicy;
    }

    void subscribe(CoreSubscriber<? super T> actual) {
        try{
            FaultExecutor.execute(governanceRequest,faultInjectionPolicy,
                (delay, sleepCallback) -> delaySubscription(actual,delay));

            if(!executed){
                source.subscribe(new FaultInjectionSubscriber<>(actual));
            }
        }catch (FaultInjectionException e){
            Operators.error(actual, e);
        }
    }

    private void delaySubscription(CoreSubscriber<? super T> actual, long waitDuration) {
        executed = true;
        Mono.delay(Duration.ofMillis(waitDuration))
            .subscribe(delay -> source.subscribe(
                new FaultInjectionSubscriber<>(actual)));
    }
}
