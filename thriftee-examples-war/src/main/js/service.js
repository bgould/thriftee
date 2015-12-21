/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var $     = require('jquery');
var util  = require('./util');
var pages = require('./pages');

var __ = {};

__.enhance_svc_ctx = (function enhance_svc_ctx(_context) {
  return _context;
});

__.enhance_svc_tpl = (function enhance_svc_tpl(ctx, content_area) {
  console.debug('enhancing service page: ', ctx, content_area);
  $('.service_method_link', content_area).click(function () {
    console.log("service method link clicked", this);
    ctx.ctx.page.app.updatePage(ctx.ctx.page, { 'method_name' : $(this).attr("rel") });
  });
});

__.enhance_method_ctx = (function enhance_method_ctx(_context) {
  return _context;
});

__.enhance_method_tpl = (function enhance_method_tpl(ctx, content_area) {
  console.debug('enhancing method: ', ctx, content_area);
  $('.service_link', content_area).click(function () {
    console.log("service link clicked", this);
    ctx.ctx.page.app.updatePage(ctx.ctx.page, {});
  });
  for (var arg_id in ctx.method.arguments) {
    var arg = ctx.method.arguments[arg_id];
    console.log('arg: ', arg);
  }
  /*
  $('#service_method_test_button').click(function () {
    var input = $('#service_method_test_input'),
        value = input.val();
    console.log("service method button clicked.");
    if (!value) {
      alert('Please enter a test message.');
    }

    return false;
  });
  */
});

__.update_content = (function update_content(ctx, content_area, callback) {
  var _context = {
    'ctx'     : ctx,
    'module'  : this.module,
    'service' : this.service,
    'methods' : this.service.methods
  };
  var method_details = false;
  if (typeof ctx.state.method_name === 'string') {
    method_details = this.service.methods[ctx.state.method_name];
  }
  var _template = null, 
      _enhance_tpl = null, 
      _enhance_ctx = null;
  if (!method_details) {
    _template = require('./service.handlebars');
    _enhance_ctx = __.enhance_svc_ctx;
    _enhance_tpl = __.enhance_svc_tpl;
  } else {
    _context.method = method_details;
    _template = require('./service_method.handlebars');
    _enhance_ctx = __.enhance_method_ctx;
    _enhance_tpl = __.enhance_method_tpl;
  }
  $(content_area).html(_template(_enhance_ctx(_context)));
  _enhance_tpl(_context, content_area);
  callback(null);
});

function ServicePage(app, module_name, service_name) {
  var module = app.client.schema.modules[module_name];
  if (!module) {
    throw new Error('invalid module name: ' + module_name);
  }
  var service = module.services[service_name];
  if (!service) {
    throw new Error('invalid service name: ' + module_name + '.' + service_name);
  }
  var full_name = module_name + '.' + service_name;
  var title = 'Service Details for ' + full_name;
  var page_name = 'service_' + module_name + "_" + service_name;
  pages.Page.call(this, app, page_name, title, __.update_content);
  Object.defineProperty(this, 'module', { 'value' : module, 'enumerable' : true });
  Object.defineProperty(this, 'service', { 'value' : service, 'enumerable' : true });
}
util.inherits(ServicePage, pages.Page)

module.exports = { ServicePage : ServicePage };
