var $ = require('jquery');
var logging = require('./logging');
var logger = logging.get('thriftee');
var util = require('./util');
var events = require('events');

logger.level = "debug";

// holder for 'private' functions
var __ = {};

// module.exports - 'public' functions
var ThriftEE = {};

// reference to a ThriftEE server
var Server = function (id, base, options) {
  var serverEvents = new events.EventEmitter();
  events.EventEmitter.call(this);
  logger.debug('options', options);
  options = util.extend(util.extend({ 
    'id'   : id, 
    'base' : base
  }), options);
  logger.debug('options', options);
  util.define(this, options);
  Object.defineProperty(this, '_events',  { 'value'  : serverEvents });
  Object.defineProperty(this, '_loading', { 'writable' : true, 'value' : 0 });
  logger.debug('created ThriftEE server instance: ', this);
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
  services.org_thriftee_compiler_schema.ThriftSchemaService.getSchema(function (schema) {
    logger.debug('loaded schema', schema);
    var schema_str = JSON.stringify(schema);
    Object.defineProperty(self, 'schema', { 'get' : function () { return JSON.parse(schema_str); } });
    for (i in schema.modules) {
      logger.debug('Found module named ', i);
      if (typeof(services[i]) === 'undefined') {
        services[i] = {};
      }
      var module = schema.modules[i];
      for (serviceName in module.services) {
        var client = window[serviceName + 'Client'];
        logger.debug("Looping over service named ", serviceName, client);
        if (typeof(client) === 'function') {
          logger.debug('Found service/client: ', serviceName, client);
          services[i][serviceName] = multiplexer.createClient(i + '.' + serviceName, client, transport);
        }
      }
    }
    callback(null, self);
  });
};
util.inherits(Server, events.EventEmitter);

Object.defineProperty(Client, 'init', {
  'value' : function(server, callback) {
    var client = new Client(server, callback);
  }
});

Server.prototype.init = function(callback) {
  var $server = this;
  logger.debug('loading: ', $server._loading);
  if ($server._loading == 2) { // loaded
    logger.debug('already loaded, firing callback');
    callback();
    return $server;
  
  } else if ($server._loading == 1) { // loading
    logger.debug('already loading, registering callback');
    $server._emitter.once('loaded', callback);
    return $server;
  } else if ($server._loading == 0) { // not started
    logger.debug('not loaded, starting');
    $server._events.once('loaded', callback);
    $server._loading = 1;
    var onError = function (jqXHR, settings, exception) {
      logger.debug("onError() fired");
      $server._events.emit('loaded', exception);
      $server._loading = 0;
    };
    var onSuccess = function () {
      logger.debug("onSuccess() fired");
      $server._loading = 2;
      $server._events.emit('loaded', null, $server);
    };
    $.get($server.base + $server.jqueryClientPath, function (data, textStatus, jqXHR) {
      var scripts = {};
      var onComplete = function (href) {
        logger.debug("finished ", href);
        if ($server._loading == 1) {
          var result = true;
          for (var i in scripts) {
            if (scripts[i].href !== href && !scripts[i].loaded) {
              logger.debug("still need to load: ", scripts[i]);
              result = false;
            }
          }
          if (!result) {
            return false;
          }
          onSuccess();
        }
      };
      var $html = $(data);
      logger.debug("getting scripts; $this._loading = ", $server._loading);
      $("a[href$='.js']", $html).not("a[href$='thrift.js']").each(function () {
        var href = $(this).attr("href");
        scripts[href] = new __.script_loader(href);
        scripts[href].load(function () {
          onComplete(href);
        });
      });
    }).fail(onError);
    return $server;
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
    logger.debug('entered createClient() ', server, Thrift);
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

// filter out any options that we've not defined as defaults and convert all values to strings
__.makeopts = function (_options) {
  var size = 0;
  var tmp = {};
  _options = _options || {};
  if (typeof(_options) !== 'object') {
    throw 'if options config is supplied, it must be an object';
  }
  for (var opt in _options) {
    if (__.defaults.hasOwnProperty(opt)) {
      tmp[opt] = (_options[opt]) ? (_options[opt] + "") : ""; // convert all values to string (immutable)
      size++;
    } else {
      logging.get('thriftee').warn('invalid configuration option (skipping): ', opt);
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
  // if only default options were provided and the instance is cached, just return it
  if (_opts.size == 0 && __.instances[base]) {
    return __.instances[base];
  }
  // merge defaults in specific order before hashing
  for (var _default in __.defaults) {
    options[_default] = _opts.options.hasOwnProperty(_default) ? _opts.options[_default] : __.defaults[_default];
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
__.script_loader = function (script) {
  if (__.scripts[script]) {
    logger.debug("found existing instance for script", script);
    return(__.scripts[script]);
  };
  __.scripts[script] = (typeof(this) !== 'object') ? new __.script_loader(script) : this;
  Object.defineProperty(this, 'script', {
    'value' : script,
    'enumerable' : true
  });
  /*
  Object.defineProperty(this, '_loading', {
    'value' : 0,
    'writeable' : true,
    'enumerable' : false
  });
  */
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

__.script_loader.prototype.load = function(callback) {
  var $this = this;
  if ($this._loading == 0) { // not started
    $this._emitter.once('loaded', callback);
    $this._loading = 1;
    $.getScript($this.script)
      .done(function (script, textStatus, jqXHR) {
        $this._loading = 2;
        $this._emitter.emit('loaded');
      })
      .fail(function (jqXHR, settings, exception) {
        logger.error('failed to retrieve script', $this, exception);
        $this._emitter.emit('loaded', exception);
        $this._loading = 0;
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
