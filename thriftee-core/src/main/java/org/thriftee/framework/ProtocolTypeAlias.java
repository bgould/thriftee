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
package org.thriftee.framework;

import org.apache.thrift.protocol.TProtocolFactory;

public class ProtocolTypeAlias {

  private final String name;

  private final TProtocolFactory inFactory;

  private final TProtocolFactory outFactory;

  public ProtocolTypeAlias(
      final String _name, 
      final TProtocolFactory _factory
    ) {
    this(_name, _factory, _factory);
  }

  public ProtocolTypeAlias(
      final String _name, 
      final TProtocolFactory _inFactory, 
      final TProtocolFactory _outFactory
    ) {
    if (_name == null) {
      throw new IllegalArgumentException("name is required.");
    }
    if (_inFactory == null) {
      throw new IllegalArgumentException("inFactory is required.");
    }
    if (_outFactory == null) {
      throw new IllegalArgumentException("outFactory is required.");
    }
    this.name = _name;
    this.inFactory = _inFactory;
    this.outFactory = _outFactory;
  }

  public String getName() {
    return name;
  }

  public TProtocolFactory getInFactory() {
    return inFactory;
  }

  public TProtocolFactory getOutFactory() {
    return outFactory;
  }

}
