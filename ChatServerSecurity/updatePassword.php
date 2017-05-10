<?php
include_once('include/config.php');	
include_once('include/AES.php');
$response=array();
if(isset($_POST['email']) && isset($_POST['newPassword']) && isset($_POST['oldPassword']) && isset ($_POST['root']) && $_POST['root']=="caputotavellamantovani99" && isset($_POST["id"])){
		
	$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME) or die( "Unable to connect");
	if(!mysqli_connect_errno()){
	
		//mi sono connesso al database
		$email=mysqli_real_escape_string($connection,$_POST["email"]);
		$query=mysqli_query($connection,"SELECT * FROM User WHERE email='{$email}'");
		//ottengo l'identificativo relativo all'utente con quella emauil
		if($query){
			$query=mysqli_fetch_assoc($query);
			$pass=mysqli_query($connection,"SELECT * FROM Chiavi WHERE id_key='{$query['id_key']}' ");
			if($pass){
				$pass=mysqli_fetch_assoc($pass);
				if($pass['password']==mysqli_real_escape_string($connection,$_POST['oldPassword'])){
					//se sono qua la vecchia password era corretta ora posso cambiarla con quella nuova
					$changePassword=mysqli_query($connection,"UPDATE Chiavi SET password = '{$_POST['newPassword']}' WHERE id_key='{$query['id_key']}'");
					if($changePassword){
						$response["errore"]=FALSE;
					}else{
						$response["errore"]=TRUE;
						$response["text"]="impossibile aggiornare la password";
			
					} 			
				}else{
				
					$response["errore"]=TRUE;
					$response["text"]="impossibile aggiornare la password, password non corrispondenti";
			
				}
			}else{
				
				$response["errore"]=TRUE;
				$response["text"]="impossibile aggiornare la password,impossibile trovare riga corrispondente";			

			}
		}else{
		

			$response["errore"]=TRUE;
			$response["text"]="impossibile aggiornare la password utente non trovato";
			
		}
	
	

	}else{

		$response["errore"]=TREE;
		$response["text"]="impossibile aggiornare la password";		
	
	}
		
	print(json_encode($response));
}


?>
