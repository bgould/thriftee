package org.thriftee.compiler.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.thriftee.compiler.schema.ModuleSchema.Builder;
import org.thriftee.util.New;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct(builder=Builder.class)
public final class ModuleSchema extends BaseSchema<ThriftSchema, ModuleSchema> {
  
  public static final int THRIFT_INDEX_NAME = 1;
  
  public static final int THRIFT_INDEX_EXCEPTIONS = THRIFT_INDEX_NAME + 1;
  
  public static final int THRIFT_INDEX_SERVICES = THRIFT_INDEX_EXCEPTIONS + 1;
  
  public static final int THRIFT_INDEX_STRUCTS = THRIFT_INDEX_SERVICES + 1;
  
  public static final int THRIFT_INDEX_UNIONS = THRIFT_INDEX_STRUCTS + 1;
  
  public static final int THRIFT_INDEX_ENUMS = THRIFT_INDEX_UNIONS + 1;

  private static final long serialVersionUID = 1973580748761800425L;

  private final Map<String, ExceptionSchema> exceptions;
  
  private final Map<String, ServiceSchema> services;
  
  private final Map<String, StructSchema> structs;
  
  private final Map<String, UnionSchema> unions;
  
  private final Map<String, EnumSchema> enums;
  
  public ModuleSchema(
      ThriftSchema _parent,
      String _name, 
      Collection<ExceptionSchema.Builder> _exceptions,
      Collection<ServiceSchema.Builder> _services, 
      Collection<StructSchema.Builder> _structs,
      Collection<UnionSchema.Builder> _unions,
      Collection<EnumSchema.Builder> _enums) throws SchemaBuilderException {
    super(ThriftSchema.class, ModuleSchema.class, _parent, _name, null);
    this.exceptions = toMap(this, _exceptions);
    this.services = toMap(this, _services);
    this.structs = toMap(this, _structs);
    this.unions = toMap(this, _unions);
    this.enums = toMap(this, _enums);
  }
  
  @ThriftField(THRIFT_INDEX_NAME)
  public String getName() {
    return super.getName();
  }
  
  @ThriftField(THRIFT_INDEX_EXCEPTIONS)
  public Map<String, ExceptionSchema> getExceptions() {
    return exceptions;
  }
  
  @ThriftField(THRIFT_INDEX_SERVICES)
  public Map<String, ServiceSchema> getServices() {
    return services;
  }

  @ThriftField(THRIFT_INDEX_STRUCTS)
  public Map<String, StructSchema> getStructs() {
    return structs;
  }
  
  @ThriftField(THRIFT_INDEX_UNIONS)
  public Map<String, UnionSchema> getUnions() {
    return unions;
  }

  @ThriftField(THRIFT_INDEX_ENUMS)
  public Map<String, EnumSchema> getEnums() {
    return enums;
  }
  
  public static final class Builder extends AbstractSchemaBuilder<ThriftSchema, ModuleSchema, ThriftSchema.Builder, ModuleSchema.Builder> {

    private final List<ExceptionSchema.Builder> exceptions = New.linkedList();
    
    private final List<ServiceSchema.Builder> services = New.linkedList();
    
    private final List<StructSchema.Builder> structs = New.linkedList();
    
    private final List<UnionSchema.Builder> unions = New.linkedList();
    
    private final List<EnumSchema.Builder> enums = New.linkedList();
    
    public Builder() throws NoArgConstructorOnlyExistsForSwiftValidationException {
      this(null);
      throw new NoArgConstructorOnlyExistsForSwiftValidationException();
    }
    
    Builder(ThriftSchema.Builder parentBuilder) {
      super(parentBuilder, ModuleSchema.Builder.class);
    }
    
    public ExceptionSchema.Builder addException(final String _name) {
      ExceptionSchema.Builder result = new ExceptionSchema.Builder(this);
      this.exceptions.add(result);
      return result.name(_name);
    }
    
    public ServiceSchema.Builder addService(final String _name) {
      ServiceSchema.Builder result = new ServiceSchema.Builder(this);
      this.services.add(result);
      return result.name(_name);
    }
    
    public StructSchema.Builder addStruct(final String _name) {
      StructSchema.Builder result = new StructSchema.Builder(this);
      this.structs.add(result);
      return result.name(_name);
    }
    
    public UnionSchema.Builder addUnion(final String _name) {
      UnionSchema.Builder result = new UnionSchema.Builder(this);
      this.unions.add(result);
      return result.name(_name);
    }
    
    public EnumSchema.Builder addEnum(final String _name) {
      EnumSchema.Builder result = new EnumSchema.Builder(this);
      this.enums.add(result);
      return result.name(_name);
    }
    
    @Override
    protected ModuleSchema _build(ThriftSchema parent) throws SchemaBuilderException {
      super._validate();
      final ModuleSchema result = new ModuleSchema(
        parent, 
        getName(), 
        exceptions,
        services,
        structs,
        unions,
        enums
      );
      return result;
    }

    @Override
    protected String[] toStringFields() {
      return new String[] { "name", "annotations", "services", "structs", "unions", "enums" };
    }

    @Override
    @ThriftConstructor
    public ModuleSchema build() throws SchemaBuilderException {
      throw new NoArgConstructorOnlyExistsForSwiftValidationException();
    }
    
  }
  
}
