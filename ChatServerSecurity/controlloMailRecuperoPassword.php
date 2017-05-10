<?php

include_once('include/config.php');	
include_once('include/AES.php');
$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME) or die( "Unable to connect");

$mail = $_POST['mail'];

$mail = str_replace("\n","",$mail);


$query = "SELECT * FROM User WHERE email = '{$mail}'";


// prima controllo se esiste gia un utente con quell'id
$result1=mysqli_query($connection,$query) or die ("NON FUNZIONA LA QUERY ");
echo mysqli_num_rows($result1);




?>