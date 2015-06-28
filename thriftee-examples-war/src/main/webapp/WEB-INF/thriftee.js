(function (is_common_js) {

// holder for 'private' functions
var __ = {};

// module.exports - 'public' functions
var ThriftEE = {};

__.get_script = function getScript(source, callback) {
  var doc = document;
  var script = doc.createElement('script');
  var prior = doc.getElementsByTagName('script')[0];
  script.async = 1;
  prior.parentNode.insertBefore(script, prior);
  script.onload = script.onreadystatechange = function( _, isAbort ) {
    if (isAbort || !script.readyState || /loaded|complete/.test(script.readyState) ) {
      script.onload = script.onreadystatechange = null;
      script = undefined;
      if (!isAbort) { 
        if (callback) {
          callback();
        }
      }
    }
  };
  script.src = source;
}; 

// reference to a ThriftEE server
function Server(id, base, options) {
  if (!(typeof(options) === 'object')) {
    options = {};
  }
  options.id = id;
  options.base = base;
  Object.defineProperty(this, '_option', { 'value' :  function (key) {
    return options[key];
  }});
  Object.defineProperty(this, '_loading', { 'writable' : true, 'value' : 0 });
};

function Client (server, callback) {
  var self = this;
  Object.defineProperty(this, 'server', {
    get : function () {
      return server;
    },
    'enumerable' : true
  });
  var multiplexer = new Thrift.Multiplexer();
  var transport = new Thrift.Transport(server.base + server.endpointPath);
  var protocol = new Thrift.Protocol(transport);
  var services = { 
    'org_thriftee_compiler_schema' : {
      'ThriftSchemaService' : multiplexer.createClient(
        'org_thriftee_compiler_schema.ThriftSchemaService', 
        ThriftSchemaServiceClient, 
        transport
      )
    }
  };
  Object.defineProperty(this, 'services', {
    get : function () {
      return services;
    },
    'enumerable' : true
  });
  services.org_thriftee_compiler_schema.ThriftSchemaService.getSchema(
    function (schema) {
      var schema_str = JSON.stringify(schema);
      Object.defineProperty(self, 'schema', {
        'get' : function () {
          return JSON.parse(schema_str);
        }
      });
      for (i in schema.modules) {
        if (typeof(services[i]) === 'undefined') {
          services[i] = {};
        }
        var module = schema.modules[i];
        for (serviceName in module.services) {
          var client = window[serviceName + 'Client'];
          if (typeof(client) === 'function') {
            services[i][serviceName] = multiplexer.createClient(
              i + '.' + serviceName, client, transport
            );
          }
        }
      }
      callback(null, self);
    }
  );
};

Object.defineProperty(Client, 'init', {
  'value' : function(server, callback) {
    var client = new Client(server, callback);
  }
});

Server.prototype.init = function(callback) {
  var _server = this;
  if (_server._loading == 2) { // loaded
    callback();
    return _server;
  } else if (_server._loading == 1) { // loading
    return _server;
  } else if (_server._loading == 0) { // not started
    _server._loading = 1;
    var onError = function (jqXHR, settings, exception) {
      _server._loading = 0;
    };
    var onSuccess = function () {
      _server._loading = 2;
      return true;
    };
    var scripts = {};
    var paths = [ 'thrift.js', 'client-jquery-all.js', ];
    var onComplete = function (href) {
      if (_server._loading == 1) {
        var result = true;
        for (var i in scripts) {
          if (scripts[i].href !== href && !scripts[i].loaded) {
            result = false;
          }
        }
        if (!result) {
          return false;
        }
        return onSuccess();
      }
    };
    var load = function (href) {
      scripts[href] = new __.loader(href);
      scripts[href].load(function () {
        onComplete(href);
      });
    };
    for (i in paths) {
      var val = paths[i];
      load(_server.base + _server.jqueryClientPath + val);
    }
    return _server;
  }
  throw 'illegal state';
};

Server.prototype.isLoaded = function () {
  return this._loading == 2;
}

/**
 * @param callback a function that takes arguments (err, client)
 */
Server.prototype.createClient = function(callback) {
  return this.init(function (err, server) {
    if (err) {
      return callback(err);
    }
    Client.init(server, function (err, client) {
      if (err) {
        return callback(err);
      }
      callback(null, client);
    });
  });
};

// default options for creating server instances
__.defaults = {
  'jqueryClientPath' : '/clients/jquery/',
  'endpointPath'   : '/endpoints/multiplex/json'
};

__.makeopts = function (_options) {
  var size = 0;
  var tmp = {};
  _options = _options || {};
  if (typeof(_options) !== 'object') {
    throw 'if options config is supplied, it must be an object';
  }
  for (var opt in _options) {
    if (__.defaults.hasOwnProperty(opt)) {
      tmp[opt] = (_options[opt]) ? (_options[opt] + "") : "";
      size++;
    } else {
      console.warn('invalid configuration option (skipping): ', opt);
    }
  }
  return { 'size' : size, 'options' : tmp }; 
};

__.init = function (base, options) {
  if (!base) {
    throw 'a base URL for the ThriftEE instance must be supplied';
  }
  return new Server(base, base, __.makeopts(options));
};

__.scripts = {};

// use this to only load 1 instance of a particular script at a time
__.loader = function (script) {
  if (__.scripts[script]) {
    return(__.scripts[script]);
  };
  __.scripts[script] = ((typeof(this)!=='object')?new __.loader(script):this);
  Object.defineProperty(this, 'script', {
    'value' : script,
    'enumerable' : true
  });
  var _loading = false;
  Object.defineProperty(this, '_loading', {
    get : function () {
      return _loading;
    },
    set : function (value) {
      _loading = value;
    }
  });
  Object.defineProperty(this, 'loaded', {
    'get' : function () {
      return this._loading == 2;
    },
    'enumerable' : true
  });
};

__.loader.prototype.load = function(callback) {
  var $this = this;
  if ($this._loading == 0) { // not started
    $this._loading = 1;
    __.get_script($this.script, function () {
      $this._loading = 2;
      callback();
    });
  } else if ($this._loading == 1) { // loading
    throw 'loader for ' + $this.script + ' is already started';
  } else if ($this._loading == 2) { // loaded
    callback();
  }
};

Object.defineProperties(ThriftEE, {
  'init' : {
    'value' : function (base, options) {
      return __.init(base, options);
    }
  },
  'get' : {
    'value' : function (key) {
      return __.get(key);
    }
  }
});

if (!is_common_js) {
  window.ThriftEE = ThriftEE;
} else {
  module.exports = ThriftEE;
}

}(typeof(module) == 'object'));
