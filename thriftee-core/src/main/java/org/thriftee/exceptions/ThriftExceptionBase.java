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
package org.thriftee.exceptions;

public class ThriftExceptionBase extends Exception implements ThriftException {

	private static final long serialVersionUID = 4753565699269577159L;

	private final ThriftMessage _thrifteeMessage;
	
	private final Object[] _arguments;
	
	@Override
	public ThriftMessage getThrifteeMessage() {
		return _thrifteeMessage;
	}
	
	@Override
	public Object[] getArguments() {
		return _arguments;
	}

	public ThriftExceptionBase(final ThriftMessage thrifteeMessage, final Object... arguments) {
		super(ThriftMessageSupport.getMessage(thrifteeMessage, arguments));
		this._thrifteeMessage = thrifteeMessage;
		this._arguments = arguments;
	}

	public ThriftExceptionBase(final Throwable cause, final ThriftMessage thrifteeMessage, final Object... arguments) {
		super(ThriftMessageSupport.getMessage(thrifteeMessage, arguments), cause);
		this._thrifteeMessage = thrifteeMessage;
		this._arguments = arguments;
	}	
	
}
