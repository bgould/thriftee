var events = require('events');

var util = require('./util');

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
var Server = function (id, base, options) {
  var serverEvents = new events.EventEmitter();
  events.EventEmitter.call(this);
  options = util.extend(util.extend({ 
    'id'   : id, 
    'base' : base
  }), options);
  util.define(this, options);
  Object.defineProperty(this, '_events',  { 'value'  : serverEvents });
  Object.defineProperty(this, '_loading', { 'writable' : true, 'value' : 0 });
};
util.inherits(Server, events.EventEmitter);

var Client = function (server, callback) {
  var self = this;
  events.EventEmitter.call(this);
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
util.inherits(Server, events.EventEmitter);

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
    _server._emitter.once('loaded', callback);
    return _server;
  } else if (_server._loading == 0) { // not started
    _server._events.once('loaded', callback);
    _server._loading = 1;
    var onError = function (jqXHR, settings, exception) {
      _server._events.emit('loaded', exception);
      _server._loading = 0;
    };
    var onSuccess = function () {
      _server._loading = 2;
      _server._events.emit('loaded', null, _server);
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

// cache for the server instances
__.instances = {};

// filter out any options that we've not defined as defaults stringify
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
  var _opts = __.makeopts(options);
  // final options merged with defaults in specific order
  options = {}; 
  // if only default options were provided and the instance is cached, return 
  if (_opts.size == 0 && __.instances[base]) {
    return __.instances[base];
  }
  // merge defaults in specific order before hashing
  for (var _default in __.defaults) {
    options[_default] = _opts.options.hasOwnProperty(_default) 
                      ? _opts.options[_default] 
                      : __.defaults[_default];
  }
  // only hash if non-default options were supplied (performance)
  var hash = (_opts.size > 0) ? require('object-hash').sha1(options) : "";
  var key = base + hash;
  if (!(__.instances[key])) {
    __.instances[key] = new Server(key, base, options);
  }
  return __.instances[key];
};

__.get = function (key) {
  if (!key) {
    throw 'key must be provided';
  }
  return __.instances[key] || null;
}

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
  Object.defineProperty(this, '_emitter', {
    'value' : new events.EventEmitter(),
    'enumerable' : false
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
    $this._emitter.once('loaded', callback);
    $this._loading = 1;
    __.get_script($this.script, function () {
      $this._loading = 2;
      $this._emitter.emit('loaded');
    });
  } else if ($this._loading == 1) { // loading
    this._emitter.on('loaded', callback);
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

module.exports = ThriftEE;
