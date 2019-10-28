package org.springframework.cloud.canary.client.hytrix;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import org.springframework.cloud.canary.client.track.CanaryTrackContext;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @Author GuoYl123
 * @Date 2019/10/24
 **/
public class CanaryHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

    public CanaryHystrixConcurrencyStrategy() {
        HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
    }

    @Override
    public <T> Callable<T> wrapCallable(Callable<T> callable) {
        return new CanaryAttributeAwareCallable<>(callable, CanaryTrackContext.getServiceName(), CanaryTrackContext.getRequestHeader());
    }

    static class CanaryAttributeAwareCallable<T> implements Callable<T> {
        private final Callable<T> delegate;
        private final String serviceName;
        private final Map<String, String> requestHeader;

        public CanaryAttributeAwareCallable(Callable<T> delegate, String serviceName, Map<String, String> requestHeader) {
            this.delegate = delegate;
            this.serviceName = serviceName;
            this.requestHeader = requestHeader;
        }

        @Override
        public T call() throws Exception {
            try {
                CanaryTrackContext.setRequestHeader(requestHeader);
                CanaryTrackContext.setServiceName(serviceName);
                return delegate.call();
            } finally {
                CanaryTrackContext.remove();
            }
        }
    }
}
