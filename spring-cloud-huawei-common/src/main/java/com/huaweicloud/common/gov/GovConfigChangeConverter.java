package com.huaweicloud.common.gov;

import java.util.Set;

import org.springframework.context.ApplicationEvent;

public interface GovConfigChangeConverter {

  Set<String> convert(ApplicationEvent event);
}
