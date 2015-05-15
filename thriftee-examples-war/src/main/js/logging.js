var console = require("console");

var Logging = {};

var _defaultLevel = 1;
var _loggers = {};
var _levels = {};

function _getLevelValue(level) {
    if (!Logging.levels.hasOwnProperty(level)) {
        console.error("invalid logger level: ", level);
        return NaN;
    }
    return Logging.levels[level];
}

function Logger(name) {
    Object.defineProperty(this, '_level', {
        'value' : _defaultLevel,
        'writable'     : true,
        'configurable' : false,
        'enumerable'   : false
    });
    Object.defineProperty(this, 'name', {
        '__proto__' : null,
        'value'     : name
    });
}

function _getLogger(name) {
    if (!_loggers[name]) {
        _loggers[name] = new Logger(name);
    }
    return _loggers[name];
}

Object.defineProperty(_levels, "debug",  { 'value' : 0, enumerable: true });
Object.defineProperty(_levels, "info",   { 'value' : 1, enumerable: true });
Object.defineProperty(_levels, "warn",   { 'value' : 2, enumerable: true });
Object.defineProperty(_levels, "error",  { 'value' : 3, enumerable: true });

Object.defineProperty(Logging, 'levels', { 'value' : _levels, enumerable: true });
Object.defineProperty(Logging, 'get',    { 'value' : _getLogger });

Object.defineProperty(Logging, 'defaultLevel', { 
    'get' : function() {
        return _defaultLevel;
    },
    'set' : function(newLevel) {
        var levelValue = _getLevelValue(newLevel);
        if (isNaN(levelValue)) {
            console.error("Tried to set invalid logging level: ", newLevel);
            return;
        }
        _defaultLevel = levelValue;
    }
});

Object.defineProperty(Logger.prototype, 'level', {
    'enumerable': true,
    'configurable' : false,
    'get' : function () {
        return this._level;
    },
    'set' : function (newLevel) {
        var levelValue = _getLevelValue(newLevel);
        if (isNaN(levelValue)) {
            console.error("Tried to set invalid logging level: ", newLevel);
            return;
        }
        this._level = _getLevelValue(newLevel);
    }
});

Logger.prototype.isAtLevel = function(level) {
    var levelValue = _getLevelValue(level);
    return !isNaN(levelValue) && (this.level <= levelValue);
};

Logger.prototype.debug = function () {
    var isAtLevel = this.isAtLevel('debug');
    if (arguments.length == 0) {
        return isAtLevel;
    }
    if (isAtLevel) {
        console.log.apply(console, arguments);
    }
};

Logger.prototype.info = function () {
    var isAtLevel = this.isAtLevel('info');
    if (arguments.length == 0) {
        return isAtLevel;
    }
    if (isAtLevel) {
        console.info.apply(console, arguments);
    }
};

Logger.prototype.warn = function () {
    var isAtLevel = this.isAtLevel('warn');
    if (arguments.length == 0) {
        return isAtLevel;
    }
    if (isAtLevel) {
        console.warn.apply(console, arguments);
    }
};

Logger.prototype.error = function () {
    var isAtLevel = this.isAtLevel('error');
    if (arguments.length == 0) {
        return isAtLevel;
    }
    if (isAtLevel) {
        console.error.apply(console, arguments);
    }
};

module.exports = Logging;
