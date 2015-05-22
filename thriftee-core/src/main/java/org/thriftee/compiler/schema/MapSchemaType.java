package org.thriftee.compiler.schema;

import com.facebook.swift.codec.ThriftProtocolType;

public class MapSchemaType extends ContainerSchemaType {

  private static final long serialVersionUID = -5613803424652950927L;

  private final ISchemaType keyType;
  
  public MapSchemaType(final ISchemaType _keyType, final ISchemaType _valueType) {
    super(_valueType);
    this.keyType = _keyType;
  }
  
  public ISchemaType getKeyType() {
    return this.keyType;
  }
  
  @Override
  public String getTypeName() {
    return toNamespacedIDL(null);
  }

  @Override
  public ThriftProtocolType getProtocolType() {
    return ThriftProtocolType.MAP;
  }

  @Override
  public String toNamespacedIDL(String namespace) {
    return "map<" + getKeyType().toNamespacedIDL(namespace) + ", " + getValueType().toNamespacedIDL(namespace) + ">";
  }

}
