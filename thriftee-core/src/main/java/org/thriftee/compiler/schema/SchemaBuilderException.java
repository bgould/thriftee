package org.thriftee.compiler.schema;

import org.thriftee.exceptions.ThriftMessage;
import org.thriftee.exceptions.ThriftSystemException;

public class SchemaBuilderException extends ThriftSystemException {

    private static final long serialVersionUID = -5281964573422287736L;

    public SchemaBuilderException(ThriftMessage thrifteeMessage, Object... arguments) {
        super(thrifteeMessage, arguments);
    }

    public SchemaBuilderException(Throwable cause, ThriftMessage thrifteeMessage, Object... arguments) {
        super(cause, thrifteeMessage, arguments);
    }

    public static enum Messages implements ThriftMessage {
        
        SCHEMA_001("Name null is for an instance of %s"),
        SCHEMA_002("Type cannot be null for `%s`"),
        SCHEMA_003("Found a duplicate name in %s - `%s`"),
        
        SCHEMA_100("Global IDL include file not found."),
        SCHEMA_101("IDL include file not found for module: `%s`"),
        SCHEMA_102("Unhandled definition type: `%s`"),
        SCHEMA_103("A problem occurred parsing generated IDL: %s"),
        
        SCHEMA_901("Integer index for enum must be non-negative"),
        ;
        
        private final String _message;
        
        private Messages(String message) {
            this._message = message;
        }
        
        @Override
        public String getCode() {
            return name();
        }

        @Override
        public String getMessage() {
            return _message;
        }
        
    }
    
}
