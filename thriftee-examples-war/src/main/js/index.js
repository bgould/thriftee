var app = require('./app');
var thriftee = require('./thriftee');
app.jQuery(document).ready(function () {
  thriftee.init('services').createClient(function (err, client) {
    app.start(client);
  });
});
module.exports = app;
