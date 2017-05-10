<?php

include_once('include/config.php');	
include_once('include/AES.php');
$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME) or die( "Unable to connect");

$id_utente = $_POST['id_utente'];
$latitudine = $_POST['latitudine'];
$longitudine =  $_POST['longitudine'];


$id_utente = str_replace("\n","",$id_utente);
//$latitudine = str_replace("\n","",$latitudine);



// prima controllo se esiste gia un utente con quell'id
$result1=mysqli_query($connection,"SELECT * FROM Posizioni WHERE id_utente='{$id_utente}'") or die ("NON FUNZIONA LA QUERY DI SELECT DENTRO ALL'if");
if(mysqli_num_rows($result1) > 0)
{
    echo"esiste gia aggiorno";
    $query = "UPDATE Posizioni SET latitudine = '{$latitudine}', `longitudine` = '{$longitudine}' WHERE id_utente = {$id_utente}";
    $result1=mysqli_query($connection,$query) or die ("NON FUNZIONA LA QUERY DI Inserimaneo DENTRO ALL'if di update ");

    

}
else
{
    echo"lo aggiungo";
    $query = "INSERT INTO Posizioni  VALUES (NULL, {$id_utente}, '{$latitudine}', '{$longitudine}', CURRENT_TIMESTAMP,'1');";
    $result1=mysqli_query($connection,$query) or die ("NON FUNZIONA LA QUERY DI Inserimaneo DENTRO ALL'if di inserimento");
}



?>