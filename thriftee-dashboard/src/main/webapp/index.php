<?php 

//header("Content-Type: text/plain");
//ini_set('html_errors', 0);

require_once "inc/common.php";

//pretty_print_r($thrift);

ob_start();
?>
    Please select an option from the left.
<?php 
show_template("Dashboard", ob_get_clean());

?>