
function Page(app, name, title, callback) {
  Object.defineProperty(this, 'app',      { 'value' : app,      'writable' : false, 'enumerable' : true  });
  Object.defineProperty(this, 'name',     { 'value' : name,     'writable' : false, 'enumerable' : true  });
  Object.defineProperty(this, 'callback', { 'value' : callback, 'writable' : false, 'enumerable' : false });
  if (typeof(title) !== 'function') {
    Object.defineProperty(this, 'title', {
      'value' : title,
      'writable' : false,
      'enumerable' : true
    });
  } else {
    Object.defineProperty(this, 'title', {
      'get' : title,
      'enumerable' : true
    });
  }
}

module.exports = { Page : Page };
