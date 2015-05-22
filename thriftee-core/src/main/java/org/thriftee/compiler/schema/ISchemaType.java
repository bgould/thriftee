package org.thriftee.compiler.schema;

import com.facebook.swift.codec.ThriftProtocolType;

public interface ISchemaType {

  public String getModuleName();
  
  public String getTypeName();
  
  public ThriftProtocolType getProtocolType();
  
  public String toNamespacedIDL(String namespace);
  
  public static class Utils {
    private Utils() {}
    public static boolean isPrimitive(ISchemaType _schemaType) {
      switch (_schemaType.getProtocolType()) {
      case BOOL:
      case BYTE:
      case DOUBLE:
      case I16: 
      case I32: 
      case I64: 
      case STRING: 
        return true;
      case MAP: 
      case SET: 
      case LIST: 
      case STRUCT: 
      case ENUM:
      case UNKNOWN:
      default:
        return false;
      }
    }
  }
}
