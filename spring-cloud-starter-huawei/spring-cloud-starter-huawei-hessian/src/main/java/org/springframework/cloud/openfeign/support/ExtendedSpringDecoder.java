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

import static org.springframework.cloud.openfeign.support.FeignUtils.getHttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpMessageConverterExtractor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;

/**
 * This class is copied from SpringDecoder, and support relection GenericArrayType.
 */
@SuppressWarnings({"all", "PMD"})
@SuppressFBWarnings
public class ExtendedSpringDecoder implements Decoder {

  private final ObjectFactory<HttpMessageConverters> messageConverters;

  private final ObjectProvider<HttpMessageConverterCustomizer> customizers;

  /**
   * @deprecated in favour of
   * {@link SpringDecoder#SpringDecoder(ObjectFactory, ObjectProvider)}
   */
  @Deprecated
  public ExtendedSpringDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
    this(messageConverters, new EmptyObjectProvider<>());
  }

  public ExtendedSpringDecoder(ObjectFactory<HttpMessageConverters> messageConverters,
      ObjectProvider<HttpMessageConverterCustomizer> customizers) {
    this.messageConverters = messageConverters;
    this.customizers = customizers;
  }

  @Override
  public Object decode(final Response response, Type type) throws IOException, FeignException {
    List<HttpMessageConverter<?>> converters = messageConverters.getObject().getConverters();
    customizers.forEach(customizer -> customizer.accept(converters));
    @SuppressWarnings({"unchecked", "rawtypes"})
    HttpMessageConverterExtractor<?> extractor = new HttpMessageConverterExtractor(type, converters);

    return extractor.extractData(new FeignResponseAdapter(response));
  }

  private final class FeignResponseAdapter implements ClientHttpResponse {

    private final Response response;

    private FeignResponseAdapter(Response response) {
      this.response = response;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
      return HttpStatus.valueOf(response.status());
    }

    @Override
    public int getRawStatusCode() throws IOException {
      return response.status();
    }

    @Override
    public String getStatusText() throws IOException {
      return response.reason();
    }

    @Override
    public void close() {
      try {
        response.body().close();
      } catch (IOException ex) {
        // Ignore exception on close...
      }
    }

    @Override
    public InputStream getBody() throws IOException {
      if (response.body() == null) {
        return null;
      }
      return response.body().asInputStream();
    }

    @Override
    public HttpHeaders getHeaders() {
      return getHttpHeaders(response.headers());
    }
  }
}
