package org.thriftee.compiler.schema;

import java.io.Serializable;

import org.json.JSONObject;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public final class ThriftAnnotation implements Serializable {

    private static final long serialVersionUID = -8982112915739811635L;

    private final String name;
    
    private final String value;
    
    public ThriftAnnotation(final String _name, final String _value) {
        this.name = _name;
        this.value = _value;
    }
    
    @ThriftField(1)
    public String getName() {
        return this.name;
    }
    
    @ThriftField(2)
    public String getValue() {
        return this.value;
    }
    
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", getName());
        obj.put("value", getValue());
        return obj;
    }
    
    public String toString() {
        return toJSON().toString();
    }
    
}
