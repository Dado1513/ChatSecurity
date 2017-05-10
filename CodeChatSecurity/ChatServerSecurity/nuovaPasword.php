<?php

include('include/config.php');
$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME) or die( "Unable to connect");
$password = $_POST['nuovaPassword'];
$password = md5($password);
$mail = $_POST['mail'];
$mail = str_replace(" ","", $mail);
$mail = mysqli_real_escape_string($connection, $mail);
echo $mail;
echo $password;
$q=mysqli_query($connection, "SELECT * FROM User WHERE email='{$mail}'") or die("non finziona query select");
$q = mysqli_fetch_assoc($q);
$keyId = $q['id_key'];
echo $keyId;
mysqli_query($connection, "UPDATE Chiavi SET password='{$password}' WHERE id_key='{$keyId}'") or die("non finziona query update");

?>