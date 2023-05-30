/*
 * Copyright 2013-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.openfeign.support;

import java.util.stream.Stream;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.MediaType;

import com.huaweicloud.hessian.HessianHttpMessageConverter;

import feign.form.spring.SpringFormEncoder;

public class ExtendedSpringEncoder extends SpringEncoder {
  public ExtendedSpringEncoder(SpringFormEncoder springFormEncoder,
      ObjectFactory<HttpMessageConverters> messageConverters,
      FeignEncoderProperties encoderProperties, ObjectProvider<HttpMessageConverterCustomizer> customizers) {
    super(springFormEncoder, messageConverters, encoderProperties, customizers);
  }

  @Override
  protected boolean binaryContentType(FeignOutputMessage outputMessage) {
    MediaType contentType = outputMessage.getHeaders().getContentType();
    return contentType == null || HessianHttpMessageConverter.HESSIAN_MEDIA_TYPE.includes(contentType) || Stream
        .of(MediaType.APPLICATION_CBOR, MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_PDF,
            MediaType.IMAGE_GIF, MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG)
        .anyMatch(mediaType -> mediaType.includes(contentType));
  }
}
