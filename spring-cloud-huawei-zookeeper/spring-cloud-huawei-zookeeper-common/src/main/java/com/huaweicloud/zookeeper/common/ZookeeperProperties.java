/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.zookeeper.common;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.convert.DurationUnit;
import org.springframework.util.Assert;

/**
 * Properties related to connecting to Zookeeper.
 */
public class ZookeeperProperties {
	public static final String PREFIX = "spring.cloud.zookeeper";

	private String connectString = "localhost:2181";

	private boolean enabled = true;

	private Integer baseSleepTimeMs = 50;

	private Integer maxRetries = 10;

	private Integer maxSleepMs = 500;

	private Integer blockUntilConnectedWait = 10;

	private TimeUnit blockUntilConnectedUnit = TimeUnit.SECONDS;

	@DurationUnit(ChronoUnit.MILLIS)
	private Duration sessionTimeout = Duration.of(60 * 1000, ChronoUnit.MILLIS);

	@DurationUnit(ChronoUnit.MILLIS)
	private Duration connectionTimeout = Duration.of(15 * 1000, ChronoUnit.MILLIS);

	public String getConnectString() {
		return this.connectString;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public Integer getBaseSleepTimeMs() {
		return this.baseSleepTimeMs;
	}

	public Integer getMaxRetries() {
		return this.maxRetries;
	}

	public Integer getMaxSleepMs() {
		return this.maxSleepMs;
	}

	public Integer getBlockUntilConnectedWait() {
		return this.blockUntilConnectedWait;
	}

	public TimeUnit getBlockUntilConnectedUnit() {
		return this.blockUntilConnectedUnit;
	}

	public void setConnectString(String connectString) {
		Assert.hasText(connectString, "spring.cloud.zookeeper.connect-string may not be empty");
		this.connectString = connectString;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setBaseSleepTimeMs(Integer baseSleepTimeMs) {
		this.baseSleepTimeMs = baseSleepTimeMs;
	}

	public void setMaxRetries(Integer maxRetries) {
		this.maxRetries = maxRetries;
	}

	public void setMaxSleepMs(Integer maxSleepMs) {
		this.maxSleepMs = maxSleepMs;
	}

	public void setBlockUntilConnectedWait(Integer blockUntilConnectedWait) {
		this.blockUntilConnectedWait = blockUntilConnectedWait;
	}

	public void setBlockUntilConnectedUnit(TimeUnit blockUntilConnectedUnit) {
		this.blockUntilConnectedUnit = blockUntilConnectedUnit;
	}

	public Duration getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(Duration sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public Duration getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(Duration connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

}
