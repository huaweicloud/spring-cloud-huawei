package org.apache.servicecomb.governance.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.governance.marker.GovHttpRequest;
import org.apache.servicecomb.governance.marker.Matcher;
import org.apache.servicecomb.governance.marker.TrafficMarker;

public class MatchHashModel {

  private GovHttpRequest govHttpRequest;

  private Map<String, Matcher> matchMap = new HashMap<>();

  private Map<String, Boolean> boolMatchMap = new HashMap<>();

  public MatchHashModel(GovHttpRequest govHttpRequest, Map<String, TrafficMarker> map,
      String serviceName, String version) {
    for (Entry<String, TrafficMarker> entry : map.entrySet()) {
      if (!entry.getValue().isCurrentService(serviceName, version)) {
        continue;
      }
      for (Matcher match : entry.getValue().getMatches()) {
        String key = entry.getKey() + "." + match.getName();
        matchMap.put(key, match);
        boolMatchMap.put(key, null);
      }
    }
    this.govHttpRequest = govHttpRequest;
  }

  public GovHttpRequest getGovHttpRequest() {
    return govHttpRequest;
  }

  public Map<String, Matcher> getMatchMap() {
    return matchMap;
  }

  public Map<String, Boolean> getBoolMatchMap() {
    return boolMatchMap;
  }
}
