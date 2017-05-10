<?php

include_once('include/config.php');	
include_once('include/AES.php');
$response=array();
if(isset($_POST["id"]) && isset($_POST["root"])&&$_POST["root"]=="caputotavellamantovani99"&& isset($_POST["state"]) &&isset($_POST["password"])){
	$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME) or die( "Unable to connect");
	if(!mysqli_connect_errno()){
		$id=$_POST["id"];
		$id=mysqli_real_escape_string($connection,$id);
		$password=$_POST["password"];
		$password=mysqli_real_escape_string($connection,$password);
		$result1=mysqli_query($connection,"SELECT * FROM User WHERE id_user='{$id}'");
			if($result1){
				$result1=mysqli_fetch_assoc($result1);
				$result2=mysqli_query($connection,"SELECT * FROM Chiavi WHERE id_key ='{$result1['id_key']}' AND password='{$password}'");
				if($result2){
					$id=mysqli_real_escape_string($connection,$_POST['id']);
					$update=mysqli_query($connection,"SELECT * FROM User WHERE id_user='$id'");
					if($update){
						$prova=mysqli_query($connection,"UPDATE User SET Stato_Personale='{$_POST['state']}' WHERE id_user='{$id}'");
						if($prova){
							$response["errore"]=false;
				
						}else{
							$response["errore"]=true;
							$response["risultato"]="impossibile aggiornamento stato";
						}
					}else{
				
						$response["errore"]=true;
						$response["risultato"]="impossibile query id";
					}
				}else{		
					$response["errore"]=true;
					$response["risultato"]="impossibile utente non esistente,p";
				}
			}else{
				$response["errore"]=true;
				$response["risultato"]="impossibile utente non esistente";
			}
	}else{
		
		$response["errore"]=true;
		$response["risultato"]="impossibile CONNESSIONE stato";
	}
	print json_encode($response);
}


?>