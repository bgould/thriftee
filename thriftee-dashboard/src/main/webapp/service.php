<?php

require_once "inc/common.php";

$service_name = isset($_GET['service']) ? $_GET['service'] : null;

if ($service_name && isset($thrift->clients->$service_name)) {
    $client = $thrift->clients->$service_name;
} else {
    $client = null;
}

ob_start();
if ($client) :
?>
    <iframe src="<?php echo htmlentities( THRIFT_PATH_CLIENT_HTML . '/' . $client->_namespace . '.html'); ?>" 
            style="overflow: hidden; border: 0;"
            class="col-md-12"
            onload="javascript:(function(obj){obj.style.height=(obj.contentWindow.document.body.scrollHeight+20)+'px';})(this);"></iframe>
<?php 
    $page_title = 'Service Definition for ' . $service_name;
else :
    header("HTTP/1.1 404 Not Found.");
?>
    <div class="alert alert-danger"><h3>Service not found!</h3></div>
<?php
    $page_title = "Not Found";
endif;
show_template($page_title, ob_get_clean());

?>
