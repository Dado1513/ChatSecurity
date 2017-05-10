<?php

	include_once('include/config.php');	
	include_once('include/AES.php');
        
        $id = $_POST['id'];
        $id = str_replace("\n","",$id);
	
        $connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME) or die( "Unable to connect");
	if(!mysqli_connect_errno())
        {
		
		
		$q = mysqli_query($connection,"SELECT * from User WHERE id_user = {$id}") or die("errore");
                $dati = mysqli_fetch_assoc($q);
               
		
		echo $dati['name'];
    


        }
	
?>