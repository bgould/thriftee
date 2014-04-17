<?php

ini_set('html_errors', 0);
header('Content-Type: text/plain');

require_once "client/thrift_client.php";

//$presidents = $thrift->client->getPresidents();
//print_r($presidents);

$svc = $thrift->clients->PresidentService;

$george = $svc->getPresidentByUniqueId(1);
print_r($george);

$presidents = $svc->getPresidents();
print_r($presidents);

?>
