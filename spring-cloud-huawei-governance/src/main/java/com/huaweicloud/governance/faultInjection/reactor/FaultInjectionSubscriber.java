package com.huaweicloud.governance.faultInjection.reactor;

import io.github.resilience4j.reactor.AbstractSubscriber;
import reactor.core.CoreSubscriber;

public class FaultInjectionSubscriber<T> extends AbstractSubscriber<T> {

    protected FaultInjectionSubscriber(CoreSubscriber<? super T> downstreamSubscriber) {
        super(downstreamSubscriber);
    }

    @Override
    public void hookOnNext(T value) {
        if (!isDisposed()) {
            downstreamSubscriber.onNext(value);
        }
    }

    @Override
    public void hookOnError(Throwable t) {
        downstreamSubscriber.onError(t);
    }

    @Override
    public void hookOnComplete() {
        downstreamSubscriber.onComplete();
    }
}
