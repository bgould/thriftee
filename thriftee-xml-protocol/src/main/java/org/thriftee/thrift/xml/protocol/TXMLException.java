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
package org.thriftee.thrift.xml.protocol;

import org.apache.thrift.protocol.TProtocolException;

public class TXMLException extends TProtocolException {

  private static final long serialVersionUID = 5007685985697860252L;

  public TXMLException() {
    super();
    // TODO Auto-generated constructor stub
  }

  public TXMLException(int type, String message, Throwable cause) {
    super(type, message, cause);
  }

  public TXMLException(int type, String message) {
    super(type, message);
  }

  public TXMLException(int type, Throwable cause) {
    super(type, cause);
  }

  public TXMLException(int type) {
    super(type);
  }

  public TXMLException(String message, Throwable cause) {
    super(message, cause);
  }

  public TXMLException(String message) {
    super(message);
  }

  public TXMLException(Throwable cause) {
    super(cause);
  }


}