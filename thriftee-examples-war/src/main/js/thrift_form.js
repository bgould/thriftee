
var $ = require('jquery');
var _ = require('underscore');

var app   = require( './app'   );
var util  = require( './util'  );

var __ = {};

function ThriftForm(app, struct_name, metadata) {
  
  var name_parser = /^(?:([a-zA-Z0-9_]+)\.)?([a-zA-Z0-9_]+)$/,
      parsed, module, struct;

  metadata = metadata || {};
  
  if (typeof(struct_name) !== 'string') {
    throw 'invalid struct name: ' + struct_name;
  }
  var parsed = name_parser.exec(struct_name);
  if (!parsed) {
    throw 'could not parse struct name: ' + struct_name;
  }
  
  module = parsed[1];
  struct = parsed[2];

  if (!module) {
    throw 'module name must be specified for struct: ' + struct_name;
  }
  
  module = app.client.schema.modules[module];
  if (!module) {
    throw 'invalid thrift module: ' + struct_name;
  }

  struct = module.structs[struct];
  if (!struct) {
    throw 'invalid thrift struct: ' + struct_name;
  }
  
  Object.defineProperty(this, "app", { 
    'value' : app,
    'enumerable' : true,
  });
  Object.defineProperty(this, "module", { 
    'value' : module,
    'enumerable' : true,
  });
  Object.defineProperty(this, "struct", { 
    'value' : struct,
    'enumerable' : true,
  });

  var _fieldInfo = null;
  Object.defineProperty(this, "fieldInfo", {
    'get' : function () {
      if (_fieldInfo == null) {
        _fieldInfo = __.build_field_info(this, metadata);
      }
      return _fieldInfo;
    },
    'enumerable' : true,
  });
  
};

function ThriftFormField(opts) {
  Object.defineProperty(this, 'name', {
    'value' : opts.name,
    'enumerable' : true
  });
  Object.defineProperty(this, 'fieldType', { 
    'value' : opts.fieldType,
    'enumerable' : true
  });
  Object.defineProperty(this, 'dataType', { 
    'value' : opts.dataType,
    'enumerable' : true
  });
  Object.defineProperty(this, 'constraint', { 
    'value' : opts.constraint,
    'enumerable' : true
  });
  Object.defineProperty(this, 'multivalued', { 
    'value' : opts.multivalued,
    'enumerable' : true
  });
  Object.defineProperty(this, 'metadata', {
    'value' : opts.metadata,
    'enumerable' : true
  });
};

Object.defineProperty(ThriftFormField.prototype, 'emitField', {
  'value' : function () {
    if (this.metadata.readonly) {
      return __.emit_readonly(this);
    }
    if (this.fieldType == 'text') {
      return __.emit_text_input(this);
    }
    if (this.fieldType == 'textarea') {
      return __.emit_textarea(this);
    }
    if (this.fieldType == 'checkbox') {
      return __.emit_checkbox(this);
    }
    if (this.dataType == 'set<string>') {
      return __.emit_tags(this);
    }
    return __.emit_readonly(this);
  },
  'enumerable' : false 
});

__.emit_checkbox = function (field) {
  var result = $('<input type="checkbox" />');
  result.attr("name", field.name);
  result.data('field_info', field);
  return result;
};

__.emit_text_input = function (field) {
  var result = $('<input type="text" />');
  result.attr("name", field.name);
  if (field.constraint == 'decimal') {
    result.attr("type", "number");
    result.attr("min",  "0");
    result.attr("step", ".01");
  } else if (field.constraint == 'integer') {
    result.attr("type", "number");
    result.attr("min",  "0");
    result.attr("step", "1");
  }
  if (field.metadata && field.metadata.required === true) {
    result.attr("required", true);
  }
  result.data('field_info', field);
  return result;
};

__.emit_textarea = function (field) {
  var result = $('<textarea />');
  result.attr("name", field.name);
  result.data('field_info', field);
  return result;
};

__.emit_tags = function (field) {
  var result = $('<input type="text" class="tags">');
  result.attr("name", field.name);
  result.data("field_info", field);
  return result;
};

__.emit_readonly = function (field) {
  return __.emit_text_input(field).attr("disabled", true);
};

__.get_field_type = function get_field_type(name, type_obj, metadata) {
  var module_name = type_obj.moduleName;
  var type_name = type_obj.typeName;
  var result = {
    'name'    : name,
    'fieldType'   : null, 
    'dataType'  : null, 
    'constraint'  : false,
    'multivalued' : false,
    'metadata'  : metadata,
  };
  if (module_name) { // this means it is a Thrift struct
    result.fieldType = 'form';
    result.dataType  = type_obj;
  } else {
    result.dataType = type_name;
    switch (type_name) {
      case 'double' :
        result.fieldType = 'text';
        result.constraint = 'decimal';
        break;
      case 'i32' :
        result.fieldType = 'text';
        result.constraint = 'integer';
        break;
      case 'string' :
        result.fieldType = 'text';
        break;
      case 'bool' :
        result.fieldType = 'checkbox';
        break;
      default:
        console.log("unhandled type: " + type_name);
    }
  }
  return new ThriftFormField(result);
};

__.build_field = function build_field(form, metadata, name, index) {
  var field_type = form.struct.fields[name].type;
  var _metadata = metadata[name] || {};
  var _field = util.extend({
    'name'   : name,
    'type'   : __.get_field_type(name, field_type, _metadata), 
    'meta'   : _metadata,
    'index'  : index,
    'module' : form.module.name,
    'struct' : form.struct.name,
  }, metadata[name] || {});
  return _field;
};

__.build_field_info = function build_field_info(form, metadata) {
  var _cols = {}, _meta = metadata, _i = 0;
  for (var _name in form.struct.fields) {
    _cols[_name] = __.build_field(form, metadata, _name, _i++);
  }
  return _cols;
};

module.exports = {
  ThriftForm : ThriftForm
};

