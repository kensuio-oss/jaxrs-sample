/*
 * Copyright 2017-2020 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.kensu.collector.utils;

import io.opentracing.Span;
import io.opentracing.tag.AbstractTag;

import java.util.logging.Logger;

public class GenericTag<T> extends AbstractTag<T> {
    private Logger logger = Logger.getLogger(GenericTag.class.getName());
    public GenericTag(String k) {
        super(k);
    }

    @Override
    public void set(Span span, T value) {
        // MockTracer introduces a recursive call...
        if (!span.getClass().getName().equals("io.opentracing.mock.MockSpan")) {
            span.setTag(this, value);
        } else {
            try {
                span.setTag(super.key, value.toString());
            } catch (IllegalStateException e) {
                logger.warning("This should only happen during tests...");
            }
        }
    }
    
}
