(function (is_common_js) {

// default options for creating server instances
var __defaults = {
      'jqueryClientPath' : '/clients/jquery/',
      'endpointPath'   : '/endpoints/multiplex/json'
    },
    __scripts = {};

// adds an element to the page for scripts that need to be dynamically loaded
function _get_script(source, callback) {
  var doc = document;
  var body = doc.getElementsByTagName('body')[0];
  var script = doc.createElement('script');
  body.appendChild(script);
  script.onload = script.onreadystatechange = function(_, abort) {
    var ready_state = script.readyState;
    if (abort || !ready_state || /loaded|complete/.test(ready_state)) {
      script.onload = script.onreadystatechange = null;
      script = undefined;
      if (!abort) { 
        if (callback) {
          callback();
        }
      } else {
        // TODO: handle error/abort
      }
    }
  };
  script.src = source;
} 

function _freeze(obj) {
  if (typeof(Object.freeze) === 'function') {
    return Object.freeze(obj);
  }
  return obj;
}

function _make_opts(_options) {
  var tmp = {}, opt;
  _options = _options || {};
  if (typeof(_options) !== 'object') {
    throw 'if options config is supplied, it must be an object';
  }
  for (opt in _options) {
    if (__defaults.hasOwnProperty(opt)) {
      tmp[opt] = (_options[opt]) ? (_options[opt] + "") : "";
    } else {
      console.warn('invalid configuration option (skipping): ', opt);
    }
  }
  for (opt in __defaults) {
    if (__defaults.hasOwnProperty(opt)) {
      if (!tmp.hasOwnProperty(opt)) {
        tmp[opt] = __defaults[opt];
      }
    }
  }
  return tmp;
}

// use this to only load 1 instance of a particular script at a time
function ScriptLoader(script) {
  if (__scripts[script]) {
    return __scripts[script];
  }
  if (typeof(this) !== 'object') {
    __scripts[script] = new ScriptLoader(script);
    return __scripts[script];
  }
  var _loader = __scripts[script] = this,
      _loading = 0;
  Object.defineProperties(_loader, {
    'script' : { 
      'value' : script, 
      'enumerable' : true
    },
    'loading' : {
      'get' : function () { return _loading === 1; },
      'enumerable' : true
    },
    'loaded' : {
      'get' : function () { return _loading === 2; },
      'enumerable' : true
    },
    'load' : {
      'value' : function load(callback) {
        if (_loader.loading) {
          throw 'loader for ' + _loader.script + ' is already started';
        } 
        if (_loader.loaded) {
          callback();
        }
        _loading = 1;
        _get_script(_loader.script, function () {
          _loading = 2;
          callback();
        });
      }
    },
  });
}

// reference to a ThriftEE server
function Server(id, base, options) {

  if (typeof(options) !== 'object') {
    options = {};
  }
  options.id = id;
  options.base = base;
  
  var _server = this,
      _loading = 0,
      _when_loaded = [],
      _paths = [],
      _scripts = {};

  function _on_loaded(callback) {
    _when_loaded.push(callback);
  }

  function _fire_loaded(err) {
    try {
      _when_loaded.forEach(function (el) {
        el(err, _server);
      });
    } finally {
      _when_loaded = [];
    }
    return null;
  }

  /*
  function _on_init_error(jqXHR, settings, exception) {
    _loading = 0;
    return _fire_loaded(exception);
  }
  */

  function _on_init_success() {
    _loading = 2;
    return _fire_loaded(null);
  }

  // TODO: choosing to download thrift.js should be configurable
  function _is_thrift_loaded() {
    return typeof(Thrift) === 'object';
  }

  // TODO: choosing to download thriftee.js should be configurable
  function _is_client_loaded() {
    return typeof(ThriftSchemaServiceClient) === 'function';
  }

  function _on_script_complete(href) {
    if (_loading !== 1) {
      throw 'illegal state: ' + _loading;
    }
    var result = true;
    for (var i in _scripts) {
      if (_scripts[i].href !== href && !_scripts[i].loaded) {
        result = false;
      }
    }
    if (!result) {
      return false;
    }
    return _on_init_success();
  }

  function _load_script(path) {
    var href = _make_href(path);
    _scripts[href] = new ScriptLoader(href);
    _scripts[href].load(function _script_completed() {
      return _on_script_complete(href);
    });
  }

  function _make_href(path) {
    return options.base + options.jqueryClientPath + path;
  }

  Object.defineProperties(_server, {
    'id' : { 'value' : id, 'enumerable' : true },
    'base' : { 'value' : base, 'enumerable' : true },
    'endpoint' : {
      'value' : options.base + options.endpointPath,
      'enumerable' : true
    },
    'loaded' : {
      'get' : function () {
        return _loading === 2;
      },
      'enumerable' : true
    },
    'loading' : {
      'get' : function (){
        return _loading === 1;
      },
      'enumerable' : true
    },
    'init' : {
      'value' : function (callback) {
        if (_server.loaded) {
          callback();
          return _server;
        }
        if (_server.loading) {
          _on_loaded(callback);
          return _server;
        }
        _loading = 1;
        _on_loaded(callback);
        if (!_is_thrift_loaded()) {
          _paths.push('thrift.js');
        }
        if (!_is_client_loaded()) {
          _paths.push('client-jquery-all.js');
        }
        if (_paths.length > 0) {
          _paths.forEach(_load_script);
          return _server;
        }
        _fire_loaded(null, _server);
        return _server;
      }
    },
    'createClient' : {
      'value' : function(callback) {
        return new Client(_server, callback);
      }
    }
  });

}

function ThriftSchema(model) {
  var _this = this;
  Object.defineProperties(_this, {
    'model' : { 'value' : model, 'enumerable' : true },
    'modules' : { 'value' : {}, 'enumerable' : true },
  });
  model.documents.forEach(function (doc) {
    var docname = doc.name;
    Object.defineProperty(_this.modules, docname, {
      'value' : {},
      'enumerable': true
    });
    Object.defineProperties(_this.modules[docname], {
      'exceptions' : { 'value' : {}, 'enumerable' : true },
      'typedefs'   : { 'value' : {}, 'enumerable' : true },
      'services'   : { 'value' : {}, 'enumerable' : true },
      'structs'    : { 'value' : {}, 'enumerable' : true },
      'unions'     : { 'value' : {}, 'enumerable' : true },
      'enums'      : { 'value' : {}, 'enumerable' : true },
    });
    doc.definitions.forEach(function (definition, index) {
      var type = (definition.exceptionDef ? 'exception' :
                 (definition.typedefDef   ? 'typedef'   :
                 (definition.serviceDef   ? 'service'   :
                 (definition.structDef    ? 'struct'    :
                 (definition.unionDef     ? 'union'     :
                 (definition.enumDef      ? 'enum'      :
                 (null)))))));
      if (!type) {
        console.warn("could not determine type for ", type, " in ", docname);
        return;
      }
      var def = definition[type + 'Def'];
      var obj = {};
      Object.defineProperty(_this.modules[docname][type + 's'], def.name, {
        'value' : obj,
        'enumerable' : true
      })
      switch (type) {
        case 'exception':
        case 'struct':
        case 'union':
          (function(){
            var fields = {};
            Object.defineProperties(obj, {
              'name' : { 'value' : def.name, 'enumerable' : true },
              'fields' : { 'value' : fields, 'enumerable' : true }
            });
            def.fields.forEach(function (fielddef) {
              Object.defineProperty(fields, fielddef.name, {
                'value' : fielddef,
                'enumerable' : true
              });
            });
          }());
          break;
        default:
          break;
      }
      _freeze(obj);
    });
    _freeze(_this.modules[docname].exceptions);
    _freeze(_this.modules[docname].typedefs);
    _freeze(_this.modules[docname].services);
    _freeze(_this.modules[docname].structs);
    _freeze(_this.modules[docname].unions);
    _freeze(_this.modules[docname].enums);
    _freeze(_this.modules[docname]);
  });
  _freeze(_this.modules);
  _freeze(_this.model);
}

function Client(server, callback) {
  var client = this;
  server.init(function () {
    var mplex = new Thrift.Multiplexer(),
        trans = new Thrift.Transport(server.endpoint),
        svcnm = 'org.thriftee.meta.idl.ThriftSchemaService',
        svccl = org.thriftee.meta.idl.ThriftSchemaServiceClient;
    mplex.createClient(svcnm, svccl, trans).getSchema(
      function on_schema_loaded(model) {
        var svcs = {};
        var schema = new ThriftSchema(model);
        var findobj = function(ctx, str) {
          if (!ctx || !str) {
            return null;
          }
          var nextdot = str.indexOf('.');
          if (nextdot < 0) {
            return ctx[str];
          }
          var thispart = str.substring(0, nextdot),
              nextpart = str.substring(nextdot + 1);
          return findobj(ctx[thispart], nextpart);
        };
        model.documents.forEach(function (doc, index) {
          var docname = doc.name;
          var ctxobj = findobj(window, docname);
          if (!ctxobj) {
            console.warn("could not find: " + docname);
            return;
          }
          Object.defineProperty(svcs, docname, {
            'value' : {},
            'enumerable' : true
          });
          doc.definitions.forEach(function (def, index) {
            if (def.serviceDef) {
              var svcname = def.serviceDef.name;
              var fullname = docname + "." + svcname;
              var svcClient = ctxobj[svcname + 'Client'];
              if (typeof(svcClient) !== 'function') {
                console.warn("service was not a 'function'", fullname);
                return;
              }
              Object.defineProperty(svcs[docname], svcname, {
                'value' : mplex.createClient(fullname, svcClient, trans),
                'enumerable' : true
              });
            }
          });
          _freeze(svcs[docname]);
        });
        Object.defineProperties(client, {
          'server' : { 'value' : server, 'enumerable' : true },
          'schema' : { 'value' : schema, 'enumerable' : true },
          'services' : { 'value' : _freeze(svcs), 'enumerable' : true }
        });
        _freeze(client);
        callback(null, client);
      }
    );
  });
}

var ThriftEE = _freeze(Object.defineProperties({}, {
  'init' : {
    'value' : function (base, options) {
      if (!base) {
        throw 'a base URL for the ThriftEE instance must be supplied';
      }
      return new Server(base, base, _make_opts(options));
    }
  }
}));

if (!is_common_js) {
  window.ThriftEE = ThriftEE;
} else {
  module.exports = ThriftEE;
}

}(typeof(module) == 'object'));
