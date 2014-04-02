/*jslint browser: true, unparam: true */
/*global jQuery, ace, console, define */

define('thriftee/ace', ['require', 'ace'], function (require, ace) {
    "use strict";

    var editor = ace.edit("code-editor"),
        themelist = require('ace/ext/themelist'),
        $ = jQuery,
        default_theme = 'ace/theme/solarized_light';

    console.log(themelist);

    editor.setTheme(default_theme);
    editor.getSession().setMode("ace/mode/javascript");

    $(function () {
        $('#code-editor-change-theme').each(function () {
            var $this = $(this);
            themelist.themes.forEach(function (x) {
                var $option = $("<option />").attr("value", x.theme).html(x.caption).appendTo($this);
                if (x.theme == default_theme) {
                    $option.attr("selected", true);
                }
            });
        }).change(function (ev) {
            var $this = $(this);
            editor.setTheme($("option:selected", $this).attr("value"));
        });
    });

});
