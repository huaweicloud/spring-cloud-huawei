package org.springframework.cloud.canary.client.ribbon;

import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PredicateKey;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * 该类是一个过滤器，起最终实现了guava的Predicate
 * @Author GuoYl123
 * @Date 2019/10/12
 **/
public class CanaryDecisionPredicate  extends AbstractServerPredicate {

    public CanaryDecisionPredicate(IRule rule) {
        super(rule);
    }

    @Override
    public boolean apply(@Nullable PredicateKey predicateKey) {
        return true;
    }
}
