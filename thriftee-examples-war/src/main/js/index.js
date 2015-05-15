// note: in this context jQuery is global, which is what we want so that it can be used by Bootstrap
var $ = jQuery = require('jquery');

var logging = require('./logging');
//logging.defaultLevel = 'debug';

var logger = logging.get('index');
logger.info('Initialized logging; starting application. Default logging level: ', logging.defaultLevel);

var thriftee = require('./thriftee');
var app = require('./app');

MGV = {};

$(document).ready(function () {
    thriftee.init('/mygunvalues-services').createClient(function (err, client) {
        logger.info('client: ', client);
        app.start(client);
    });
});

MGV = app;

// thriftee.init('/mygunvalues-services');
