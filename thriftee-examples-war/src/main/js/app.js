var $           = require('jquery');
var events      = require('events');
var object_hash = require('object-hash');

var util        = require('./util');
var pages       = require('./pages');

var __ = {};
var App = {};

App.jQuery = $;

var __pages = {};
var __page = null;
var __client = null;
var __app_started = false;
var __app_events = new events.EventEmitter();

__.updating_state = false;
__.reloading_page = false;

function AppContext(page, state) {
  events.EventEmitter.call(this);
  Object.defineProperties(this, {
    'page'   : { 'value' : page,   'enumerable' : true },
    'client' : { 'value' : __client, 'enumerable' : true }
  });
  var _state = state;
  Object.defineProperty(this, 'state', {
    'get' : function () {
      return _state;
    },
    'set' : function (updated_state) {
      var current_state = _state,
          current_hash  = object_hash(current_state),
          updated_hash  = object_hash(updated_state);
      if (current_hash == updated_hash) {
        console.debug('"set" called for ctx.state with existing value');
        return;
      }   
      var obj = { 'page' : this.page.name, 'state' : updated_state };
      var srl = __.serialize_page_state(this.page.name, updated_state);
      history.replaceState(obj, page.title, '#' + srl);
      _state = updated_state;
      __app_events.emit('stateUpdated', current_state, updated_state);
    },
    'enumerable' : true,
  });
}
util.inherits(AppContext, events.EventEmitter);

Object.defineProperty(AppContext.prototype, 'destroy', {
  'value' : function() {
    this.emit('destroy');
  },
  'enumerable' : true
});

__.define_page = function (page) {
  if (!(page instanceof pages.Page)) {
    console.error('not an instance of page: ', page);
    return false;
  }
  if (typeof(page.name) !== 'string' || !(page.name)) {
    console.error('invalid page name: ', page.name);
    return false;
  }
  if (__pages[page.name]) {
    console.error(
      'there is already a page defined with that name: ', 
      page.name
    );
    return false;
  }
  return (__pages[page.name] = page);
};

__.get_page = function (page) {
  var _name = null;
  if (page instanceof pages.Page) {
    _name = page.name;
  }
  if (typeof(page) === 'string') {
    _name = page;
  }
  if (_name) {
    return __pages[_name];
  }
  return null;
};

__.define_event = function(eventName) {
  __app_events.addListener(eventName, function () {
    console.debug('[event fired] ' + eventName + ': ', arguments);
  });
};

__.start = function (client) {
  if (__app_started) {
    console.warn("application has already be started.");
    return true;
  }
  
  __client = client;

  Object.defineProperty(App, 'client', {
    'value' : __client,
    'enumerable' : true
  });
  
  // TODO: create an event hook that applications can use for 
  var dashboard = require( './dashboard' );
  var services  = require( './service' );
  __.define_page(new dashboard.DashboardPage(App));
  var service_menu = [];
  var schema = __client.schema;
  for (var module in schema.modules) {
    for (var service in schema.modules[module].services) {
      var page = new services.ServicePage(App, module, service);
      __.define_page(page);
      service_menu.push({ 
        name: service, 
        full_name: module + '.' + service,
        module: module,
        page: page
      });
    }
  }
  __.update_menu(service_menu);
  // end of section that should be handled in callbacks

  function _update_from_state(state) {
    if (state && state.page) {
      var _page = __.get_page(state.page);
      if (_page != null) {
        var _state = state.state || {};
        __.update_page(_page, _state);
      }
    } else {
      __.reload_page_from_hash();
    }
  }

  __.reloading_state = true;
  if (history.state) {
    _update_from_state(history.state);
  } else {
    __.reload_page_from_hash();
  }
  __.reloading_state = false;

  $(window).on('popstate', function (ev) {
    __.reloading_state = true;
    _update_from_state(ev.originalEvent.state);
    __.reloading_state = false;
  });

  __app_started = true;
  
  return true;
};

__.update_menu = function (service_menu) {
  var $service_sidebar = $('#services-sidebar').empty();
  $.each(service_menu, function (index, menu_item) {
    var name = menu_item.name;
    var href = __.serialize_page_state(__.get_page(menu_item.page), {});
    var $li = $("<li />").appendTo($service_sidebar);
    var $a  = $("<a />").appendTo($li);
    $a.attr("href", '#' + href);
    $a.attr("title", "Browse " + name);
    $a.text(name);
  });
  __app_events.emit('menuUpdated', service_menu);
};
__.define_event('menuUpdated');

__.update_title = function (new_title) {
  var $page_header = $('#page_header');
  var $title = $('head title');
  $page_header.text(new_title);
  $title.text(new_title + " - Dashboard");
  __app_events.emit('titleUpdated', new_title);
};
__.define_event('titleUpdated');

__.update_content = function (content) {
  var $content_area = $('#content_area');
  __app_events.emit('beforeContentUpdated', $content_area.get(0));
  if (typeof(content) === 'function') {
    content($content_area.get(0));
  } else {
    $content_area.html(content);
  }
  __app_events.emit('contentUpdated', $content_area.get(0));
};
__.define_event('beforeContentUpdated');
__.define_event('contentUpdated');

__.uri_encode = function (s) {
  return encodeURIComponent(encodeURIComponent(s));
};

__.serialize_page_state = function (page, state) {
  var _page = __.get_page(page);
  if (_page) {
    var encoded_state = _page.name + '&' + util.serialize(state);
    return encoded_state;
  } else {
    return '';
  }
};

__.deserialize_state_from_hash = function () {
  return __.deserialize_page_state(__.get_serialized_state_from_hash());
};

__.deserialize_page_state = function (serialized_value) {
  var _parts = /^([a-zA-Z0-9_\-]+)&(.+)$/.exec(serialized_value);
  if (_parts) {
    var _page      = __.get_page(_parts[1]);
    var _state_str = _parts[2] ? _parts[2] : '';
    var _state     = (_state_str && _state_str != '{}') 
                   ? util.deserialize(_parts[2]) 
                   : { };
    if (!_page) {
      log.warn('invalid page: ', _parts[0]);
    } else {
      return { 'page' : _page, 'state' : _state };
    }
  }
  return false;
};

__.reload_page_from_hash = function () {
  var _hash = __.get_serialized_state_from_hash();
  if (_hash) {
    if (__.reload_page_from_serialized(_hash)) {
      return true;
    }
  }
  __.update_page(__.get_page('dashboard'));
  return false;
};

__.get_serialized_state_from_hash = function () {
  var _hash = window.location.hash;
  if (_hash) {
    _hash = _hash.substring(1);
    return _hash;
  }
  return null;
}

__.reload_page_from_serialized = function (serialized_value) {
  var _obj = __.deserialize_page_state(serialized_value);
  if (_obj) {
    if (__.update_page(_obj.page, _obj.state)) {
      return true;
    }
  }
  return false;
};

__.update_page = function (page, state) {
  var _page  = __.get_page(page);
  var _state = state || {};
  if (_page) {
    if (__.current_context) {
      __.current_context.destroy();
      if (!__.reloading_state) {
        var serialized = __.serialize_page_state(_page.name, _state);
        history.pushState(
          { 'page' : _page.name, 'state' : _state }, 
          _page.title, 
          '#' + serialized
        );
      }
    }
    __.current_context = null;
    var $content_area = $('#content_area').empty();
    var _ctx = new AppContext(_page, _state);
    __.current_context = _ctx;
    __.update_title(_page.title);
    __.update_content(function (content_area) {
      _page.callback(_ctx, content_area, function (err) {
        if (err) return __.handle_error(err);
        __app_events.emit('pageUpdated', _page.name, state);
      });
    });
    return true;
  } else {
    console.warn('could not find page: ', pagename);
  }
};
__.define_event('pageUpdated');
__.define_event('stateUpdated');

__.current_context = null;

__.handle_error = function (err) {
  throw err;
};

Object.defineProperties(App, {
  'start' : { 'enumerable' : true, 'value' : function (client) {
    __.start(client);
  }},
  'updatePage' :  { 'enumerable' : true, 'value' : function (page, state) {
    return __.update_page(page, state);
  }},
  'jQuery' : { 'enumerable' : true, 'value' : $ }
});

module.exports = App;
