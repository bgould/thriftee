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
package org.thriftee.compiler.schema;

import com.facebook.swift.codec.ThriftProtocolType;

public class ReferenceSchemaType implements ISchemaType {

    private final ThriftProtocolType protocolType;
    
    private final String moduleName;
    
    private final String typeName;
    
    public static ReferenceSchemaType referTo(ThriftProtocolType protocolType, String moduleName, String typeName) {
        return new ReferenceSchemaType(protocolType, moduleName, typeName);
    }
    
    protected ReferenceSchemaType(ThriftProtocolType protocolType, String moduleName, String typeName) {
        super();
        this.protocolType = protocolType;
        this.moduleName = moduleName;
        this.typeName = typeName;
    }

    @Override
    public String getModuleName() {
        return this.moduleName;
    }

    @Override
    public String getTypeName() {
        return this.typeName;
    }

    @Override
    public ThriftProtocolType getProtocolType() {
        return this.protocolType;
    }

    @Override
    public String toNamespacedIDL(String namespace) {
        if (namespace != null && getModuleName() != null && namespace.equals(getModuleName())) {
            return getTypeName();
        } else {
            return getModuleName() + "." + getTypeName();
        }
    }

}
