<?php 

require_once "client/thrift_client.php";

function pretty_print_r($something) {
    echo '<pre>' . htmlentities(print_r($something, true)) . '</pre>';
}

function show_template($page_title, $page_content) {
    include("template.php");    
}

?>