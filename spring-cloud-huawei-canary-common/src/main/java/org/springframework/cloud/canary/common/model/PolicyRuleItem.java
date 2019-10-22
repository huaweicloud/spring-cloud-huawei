package org.springframework.cloud.canary.common.model;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class PolicyRuleItem implements Comparable<PolicyRuleItem> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyRuleItem.class);

    private Integer precedence;

    private Matcher match;

    // any match 只要version符合就算符合匹配规则
    private List<RouteItem> route;

    private Integer total;

    private boolean weightLess = false;

    public PolicyRuleItem() {
    }

    /**
     * 如果weight和小于100，用latestVersion补充
     *
     * @param latestVersionTag
     */
    public void check(TagItem latestVersionTag) {
        if (CollectionUtils.isEmpty(route)) {
            LOGGER.error("canary rule list can not be null");
            throw new RuntimeException("canary rule list can not be null");
        }
        if (route.size() == 1) {
            route.get(0).setWeight(100);
            return;
        }
        int sum = 0;
        for (RouteItem item : route) {
            if (item.getWeight() == null) {
                LOGGER.error("canary rule weight can not be null");
                throw new RuntimeException("weight can not be null");
            }
            sum += item.getWeight();
        }
        if (sum > 100) {
            LOGGER.warn("canary rule weight sum is more than 100");
        } else if (sum < 100) {
            if (latestVersionTag == null) {
                LOGGER.warn("canary has some error when set default latestVersion");
            }
            weightLess = true;
            route.add(new RouteItem(100 - sum, latestVersionTag));
        }
        Collections.sort(route);
    }

    @Override
    public int compareTo(PolicyRuleItem param) {
        if (param.precedence == this.precedence) {
            return 0;
        }
        return param.precedence > this.precedence ? 1 : -1;
    }

    public Integer getPrecedence() {
        return precedence;
    }

    public void setPrecedence(Integer precedence) {
        this.precedence = precedence;
    }

    public Matcher getMatch() {
        return match;
    }

    public void setMatch(Matcher match) {
        this.match = match;
    }

    public List<RouteItem> getRoute() {
        return route;
    }

    public void setRoute(List<RouteItem> route) {
        this.route = route;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public boolean isWeightLess() {
        return weightLess;
    }

    public void setWeightLess(boolean weightLess) {
        this.weightLess = weightLess;
    }

    @Override
    public String toString() {
        return "PolicyRuleItem{" +
                "precedence=" + precedence +
                ", match=" + match +
                ", route=" + route +
                ", total=" + total +
                ", weightLess=" + weightLess +
                '}';
    }
}
