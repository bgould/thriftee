/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thriftee.core;

import java.util.concurrent.ConcurrentHashMap;

import org.thriftee.core.ServiceLocatorException.Messages;

public class DefaultServiceLocator implements ServiceLocator {

  private final ConcurrentHashMap<Class<?>, Object> services;

  public DefaultServiceLocator() {
    this.services = new ConcurrentHashMap<>();
  }

  public <I> void register(Class<I> i, I svc) throws ServiceLocatorException {
    if (this.services.putIfAbsent(i, svc) != null) {
      throw new ServiceLocatorException(Messages.SVCLOC_001, i);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <I> I locate(Class<I> svcIntf) throws ServiceLocatorException {
    return (I) services.get(svcIntf);
  }

}
