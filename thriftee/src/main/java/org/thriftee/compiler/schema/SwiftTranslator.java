package org.thriftee.compiler.schema;

import java.util.List;

import com.facebook.swift.codec.ThriftProtocolType;
import com.facebook.swift.parser.model.BaseType;
import com.facebook.swift.parser.model.Definition;
import com.facebook.swift.parser.model.Document;
import com.facebook.swift.parser.model.IdentifierType;
import com.facebook.swift.parser.model.IntegerEnum;
import com.facebook.swift.parser.model.IntegerEnumField;
import com.facebook.swift.parser.model.ListType;
import com.facebook.swift.parser.model.MapType;
import com.facebook.swift.parser.model.Service;
import com.facebook.swift.parser.model.SetType;
import com.facebook.swift.parser.model.Struct;
import com.facebook.swift.parser.model.ThriftException;
import com.facebook.swift.parser.model.ThriftField;
import com.facebook.swift.parser.model.ThriftMethod;
import com.facebook.swift.parser.model.ThriftType;
import com.facebook.swift.parser.model.Union;
import com.facebook.swift.parser.model.VoidType;

public class SwiftTranslator {
    
    public static ModuleSchema.Builder translate(ThriftSchema.Builder parentBuilder, String _name, Document _document) 
            throws SchemaBuilderException {
        final ModuleSchema.Builder val = parentBuilder.addModule(_name);
        final List<Definition> definitions = _document.getDefinitions();
        for (final Definition definition : definitions) {
            if (definition instanceof Service) {
                translate(val, (Service) definition);
            } else if (definition instanceof Struct) {
                translate(val, (Struct) definition);
            } else if (definition instanceof Union) {
                translate(val, (Union) definition);
            } else if (definition instanceof IntegerEnum) {
                translate(val, (IntegerEnum) definition);
            } else if (definition instanceof ThriftException) {
                //translate(val, (ThriftException) definition);
            } else {
                throw new SchemaBuilderException(
                    SchemaBuilderException.Messages.SCHEMA_102, 
                    definition.getClass()
                );
            }
        }
        return val;
    }

    public static UnionSchema.Builder translate(final ModuleSchema.Builder parentBuilder, final Union _union) {
        final UnionSchema.Builder val = parentBuilder.addUnion(_union.getName());
        final List<ThriftField> fields = _union.getFields();
        for (int i = 0, c = fields.size(); i < c; i++) {
            final ThriftField field = fields.get(i);
            translateField(val, field);
        }
        return val;
    }
    
    public static StructSchema.Builder translate(final ModuleSchema.Builder parentBuilder, final Struct _struct) {
        final StructSchema.Builder val = parentBuilder.addStruct(_struct.getName());
        final List<ThriftField> fields = _struct.getFields();
        for (int i = 0, c = fields.size(); i < c; i++) {
            final ThriftField field = fields.get(i);
            translateField(val, field);
        }
        return val;
    }
    
    public static EnumSchema.Builder translate(ModuleSchema.Builder parentBuilder, IntegerEnum _enum) {
        EnumSchema.Builder val = parentBuilder.addEnum(_enum.getName());
        final List<IntegerEnumField> fields = _enum.getFields();
        for (int i = 0, c = fields.size(); i < c; i++) {
            IntegerEnumField field = fields.get(i);
            translate(val, field);
        }
        return val;
    }
    
    public static EnumValueSchema.Builder translate(EnumSchema.Builder parentBuilder, IntegerEnumField _field) {
        EnumValueSchema.Builder val = parentBuilder.addEnumValue(_field.getName());
        if (_field.getExplicitValue().isPresent()) {
            // TODO: support explicit values for enum fields in schema model
            throw new UnsupportedOperationException("explicit value not yet supported");
        }
        return val;
    }
    
    public static ServiceSchema.Builder translate(ModuleSchema.Builder parentBuilder, Service _service) {
        ServiceSchema.Builder val = parentBuilder.addService(_service.getName());
        if (_service.getParent().isPresent()) {
            // TODO: support parent services in schema model
            throw new UnsupportedOperationException("Parent services not yet supported.");
        }
        final List<ThriftMethod> methods = _service.getMethods();
        for (int i = 0, c = methods.size(); i < c; i++) {
            ThriftMethod method = methods.get(i);
            translate(val, method);
        }
        return val;
    }

    public static MethodSchema.Builder translate(ServiceSchema.Builder parentBuilder, ThriftMethod _method) {
        MethodSchema.Builder val = parentBuilder.addMethod(_method.getName())
                                                .oneway(_method.isOneway())
                                                .returnType(translate(_method.getReturnType()));
        final List<ThriftField> arguments = _method.getArguments(); 
        for (int i = 0, c = arguments.size(); i < c; i++) {
            ThriftField field = arguments.get(i);
            translateArgument(val, field);
        }
        final List<ThriftField> exceptions = _method.getThrowsFields(); 
        for (int i = 0, c = exceptions.size(); i < c; i++) {
            ThriftField field = exceptions.get(i);
            translateException(val, field);
        }
        return val;
    }
    
    public static StructFieldSchema.Builder translateField(StructSchema.Builder parentBuilder, ThriftField _field) {
        StructFieldSchema.Builder field = parentBuilder.addField(_field.getName());
        _translate(field, _field);
        return field;
    }
    
    public static UnionFieldSchema.Builder translateField(UnionSchema.Builder parentBuilder, ThriftField _field) {
        UnionFieldSchema.Builder field = parentBuilder.addField(_field.getName());
        _translate(field, _field);
        return field;
    }
        
    public static ArgumentSchema.Builder translateArgument(MethodSchema.Builder parentBuilder, ThriftField field) {
        ArgumentSchema.Builder arg = parentBuilder.addArgument(field.getName());
        _translate(arg, field);
        return arg;
    }
    
    public static ArgumentSchema.Builder translateException(MethodSchema.Builder parentBuilder, ThriftField field) {
        ArgumentSchema.Builder exc = parentBuilder.addException(field.getName());
        _translate(exc, field);
        return exc;
    }
    
    private static <T extends AbstractFieldSchema.AbstractFieldBuilder<?, ?, ?, ?>> T _translate(T arg, ThriftField field) {
        if (field.getIdentifier().isPresent()) {
            arg.identifier(field.getIdentifier().get());
        }
        switch (field.getRequiredness()) {
        case REQUIRED:
            arg.required(Boolean.TRUE);
            break;
        case OPTIONAL:
            arg.required(Boolean.FALSE);
            break;
        default:
            arg.required(null);
            break;
        }
        arg.type(translate(field.getType()));
        return arg;
    }
    
    public static ISchemaType translate(ThriftType thriftType) {
        if (thriftType instanceof IdentifierType) {
            // TODO: this will definitely need work
            return ReferenceSchemaType.referTo(
                ThriftProtocolType.UNKNOWN, 
                null, 
                ((IdentifierType) thriftType).getName()
            );
        } else if (thriftType instanceof MapType) {
            return new MapSchemaType(
                translate(((MapType) thriftType).getKeyType()), 
                translate(((MapType) thriftType).getValueType())
            );
        } else if (thriftType instanceof ListType) {
            return new ListSchemaType(
                translate(((ListType) thriftType).getElementType())
            );
        } else if (thriftType instanceof SetType) {
            return new SetSchemaType(
                translate(((SetType) thriftType).getElementType())
            );
        } else if (thriftType instanceof BaseType) {
            switch (((BaseType) thriftType).getType()) {
            case BOOL:
                return PrimitiveTypeSchema.BOOL;
            case BYTE:
                return PrimitiveTypeSchema.BYTE;
            case I16:
                return PrimitiveTypeSchema.I16;
            case I32:
                return PrimitiveTypeSchema.I32;
            case I64:
                return PrimitiveTypeSchema.I64;
            case DOUBLE:
                return PrimitiveTypeSchema.DOUBLE;
            case STRING:
                return PrimitiveTypeSchema.STRING;
            case BINARY:
                return PrimitiveTypeSchema.BINARY;
            default:
                throw new IllegalStateException(
                    "unknown BaseType: " + ((BaseType) thriftType).getType());
            }
        } else if (thriftType instanceof VoidType) {
            return PrimitiveTypeSchema.VOID;
        } else {
            throw new IllegalStateException(
                    "unhandled type: " + thriftType.toString());
        }
    }
    
}
