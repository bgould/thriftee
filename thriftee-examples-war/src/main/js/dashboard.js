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
