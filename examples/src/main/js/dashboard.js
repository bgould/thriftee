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

function updateContent(ctx, content_area, callback) {
  var $content_area = $(content_area);
  $content_area.html('<h3>Please choose an option from the menu on the left.</h3>');
  callback();
}

function DashboardPage(app) {
  pages.Page.call(this, app, 'dashboard', 'Dashboard', updateContent);
}
util.inherits(DashboardPage, pages.Page)

module.exports = { DashboardPage : DashboardPage }; 
