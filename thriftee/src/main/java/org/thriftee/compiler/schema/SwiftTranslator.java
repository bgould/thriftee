package org.thriftee.compiler.schema;

import java.util.List;

import com.facebook.swift.codec.ThriftProtocolType;
import com.facebook.swift.parser.model.BaseType;
import com.facebook.swift.parser.model.IdentifierType;
import com.facebook.swift.parser.model.ListType;
import com.facebook.swift.parser.model.MapType;
import com.facebook.swift.parser.model.SetType;
import com.facebook.swift.parser.model.ThriftField;
import com.facebook.swift.parser.model.ThriftMethod;
import com.facebook.swift.parser.model.ThriftType;
import com.facebook.swift.parser.model.VoidType;

public class SwiftTranslator {

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
    
    public static ArgumentSchema.Builder translateArgument(MethodSchema.Builder parentBuilder, ThriftField field) {
        ArgumentSchema.Builder arg = parentBuilder.addArgument(field.getName());
        translate(arg, field);
        return arg;
    }
    
    public static ArgumentSchema.Builder translateException(MethodSchema.Builder parentBuilder, ThriftField field) {
        ArgumentSchema.Builder exc = parentBuilder.addException(field.getName());
        translate(exc, field);
        return exc;
    }
    
    private static ArgumentSchema.Builder translate(ArgumentSchema.Builder arg, ThriftField field) {
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
            //case BINARY:
                //return PrimitiveTypeSchema.BINARY;
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
