<?php

include_once('include/config.php');	
include_once('include/AES.php');
$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME) or die( "Unable to connect");

$id_utente = $_POST['id_utente'];
$valore = $_POST['valore'];
$id_utente = str_replace("\n","",$id_utente);
$valore = str_replace("\n","",$valore);

$query = "UPDATE Posizioni SET localizzabile='{$valore}' WHERE id_utente = {$id_utente}";


// prima controllo se esiste gia un utente con quell'id
$result1=mysqli_query($connection,$query) or die ("NON FUNZIONA LA QUERY ");




?>