/*jslint browser: true, unparam: true */
/*global jQuery, console, require, define */

require.config({
    baseUrl: 'assets/js',
    map : {
        '*' : {
            'jquery' : 'jquery/jquery',
            'bootstrap' : 'bootstrap/bootstrap.min'
        }
    },
    paths : {
        bootstrap: '../bootstrap/js'
    }
});

define(['require', 'jquery', 'ace/ace', 'ace/ext/themelist', 'bootstrap'], function (require, jquery, ace) {
    "use strict";

    var $ = jQuery,
        themelist = ace.require('ace/ext/themelist'),
        editor = ace.edit("code-editor"),
        default_theme = 'ace/theme/solarized_light';

    editor.setTheme(default_theme);
    editor.getSession().setMode("ace/mode/javascript");

    $(function () {
        $('#code-editor-change-theme').each(function () {
            var $this = $(this);
            themelist.themes.forEach(function (x) {
                var $option = $("<option />").attr("value", x.theme).html(x.caption).appendTo($this);
                if (x.theme === default_theme) {
                    $option.attr("selected", true);
                }
            });
        }).change(function (ev) {
            var $this = $(this);
            editor.setTheme($("option:selected", $this).attr("value"));
        });
    });

});
