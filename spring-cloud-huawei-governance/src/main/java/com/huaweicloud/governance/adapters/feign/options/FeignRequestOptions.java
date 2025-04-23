/*

 * Copyright (C) 2020-2025 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.governance.adapters.feign.options;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeignRequestOptions {
    /**
     * request connect timeout, default 10 second
     *
     * @return request connect timeout
     */
    long connectTimeout() default 10L;

    /**
     * request connect timeout unit, default second
     *
     * @return request connect timeout unit
     */
    TimeUnit connectTimeoutUnit() default TimeUnit.SECONDS;

    /**
     * response timeout, default 60 second
     *
     * @return response timeout
     */
    long readTimeout() default 60L;

    /**
     * request connect timeout unit, default second
     *
     * @return request connect timeout unit
     */
    TimeUnit readTimeoutUnit() default TimeUnit.SECONDS;

    /**
     * is follow redirects, default true
     *
     * @return is follow redirects
     */
    boolean followRedirects() default true;
}
