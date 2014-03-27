/*jslint browser: true */
/*global jQuery, ace, console, define */

define('thriftee/ace', ['require', 'exports', 'module' , 'ace/lib/fixoldbrowsers', 'ace/multi_select', 'ace/ext/spellcheck', 'ace/config', 'ace/lib/dom', 'ace/lib/net', 'ace/lib/lang', 'ace/lib/useragent', 'ace/lib/event', 'ace/theme/textmate', 'ace/edit_session', 'ace/undomanager', 'ace/keyboard/hash_handler', 'ace/virtual_renderer', 'ace/editor', 'ace/ext/whitespace', 'ace/ext/modelist', 'ace/ext/themelist', 'ace/ext/elastic_tabstops_lite', 'ace/incremental_search', 'ace/worker/worker_client', 'ace/split', 'ace/keyboard/vim', 'ace/ext/statusbar', 'ace/ext/emmet', 'ace/snippets', 'ace/ext/language_tools'], function(require, exports, module) {

    "use strict";
    var editor = ace.edit("code-editor"),
        themelist = ace.require('ace/ext/themelist');

    //console.log(themelist);

    editor.setTheme("ace/theme/monokai");
    //editor.getSession().setMode("ace/mode/javascript");

    function create_ace_sidebar() {
        var $theme_selector = $('#code-editor-change-theme');
       // themelist.themes.forEach(function (x) { console.log(x); });
    }

    $(function () {
        create_ace_sidebar();
    });

});
