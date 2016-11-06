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
