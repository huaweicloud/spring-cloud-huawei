package org.springframework.cloud.canary.core.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class ServiceInfoCache {

  private List<PolicyRuleItem> allrule;
  //用于default的情况
  private TagItem latestVersionTag;

  public ServiceInfoCache() {
  }

  public void sortRule() {
    allrule = allrule.stream().sorted().collect(Collectors.toList());
  }

  public TagItem getNextInvokeVersion(PolicyRuleItem policyRuleItem) {
    List<RouteItem> rule = policyRuleItem.getRoute();
    if (policyRuleItem.getTotal() == null) {
      policyRuleItem.setTotal(rule.stream().mapToInt(RouteItem::getWeight).sum());
    }
    rule.stream().forEach(RouteItem::addCurrentWeight);
    int maxIndex = 0, maxWeight = -1;
    for (int i = 0; i < rule.size(); i++) {
      if (maxWeight < rule.get(i).getCurrentWeight()) {
        maxIndex = i;
        maxWeight = rule.get(i).getCurrentWeight();
      }
    }
    rule.get(maxIndex).reduceCurrentWeight(policyRuleItem.getTotal());
    return rule.get(maxIndex).getTagitem();
  }

  public List<PolicyRuleItem> getAllrule() {
    return allrule;
  }

  public void setAllrule(List<PolicyRuleItem> allrule) {
    this.allrule = allrule;
  }

  public TagItem getLatestVersionTag() {
    return latestVersionTag;
  }

  public void setLatestVersionTag(TagItem latestVersionTag) {
    this.latestVersionTag = latestVersionTag;
  }

  @Override
  public String toString() {
    return "ServiceInfoCache{" +
        "allrule=" + allrule +
        ", latestVersionTag=" + latestVersionTag +
        '}';
  }
}
