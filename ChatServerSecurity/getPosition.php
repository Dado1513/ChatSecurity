<?php

	include_once('include/config.php');	
	include_once('include/AES.php');
	
        $connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME) or die( "Unable to connect");
	if(!mysqli_connect_errno())
        {
		
		$response=array();
		$q = mysqli_query($connection,"SELECT * from Posizioni WHERE localizzabile = '1'") or die("errore");
                while($dati = mysqli_fetch_assoc($q))
                {
                    $out[] = $dati;
                }
		
		print(json_encode($out));
    


        }
	
?>