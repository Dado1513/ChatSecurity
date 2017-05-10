<?php

include_once('include/config.php');
$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME);
$idMessaggio = $_POST['id_messaggio'];

$idMessaggio = mysqli_real_escape_string($connection, $idMessaggio);


$q = mysqli_query($connection, "SELECT * FROM Message WHERE id_message = '{$idMessaggio}' ") or die("mantovani");
$q = mysqli_fetch_assoc($q);
print $q['text'];

?>