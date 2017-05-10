<?php
include_once('include/config.php');	
include_once('include/AES.php');
$response=array();
if(isset($_POST['email']) && isset($_POST['password']) && isset ($_POST['root']) && $_POST['root']=="caputotavellamantovani99"){
	$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME) or die( "Unable to connect");
	if(!mysqli_connect_errno()){
	
		$email=mysqli_real_escape_string($connection,$_POST["email"]);
		$query=mysqli_query($connection,"SELECT * FROM User WHERE email='{$email}'");
		if($query){
			$query=mysqli_fetch_assoc($query);
			$pass=mysqli_query($connection,"SELECT * FROM Chiavi WHERE id_key='{$query['id_key']}' ");
			if($pass){
				$pass=mysqli_fetch_assoc($pass);
				if($pass['password']==mysqli_real_escape_string($connection,$_POST['password'])){
					
					$aes1 = new AES($_POST['token'],"caputotavellamantovani99", 256);	
					$token = $aes1->encrypt();
					
					//if($pass["gcmRegistration"]==null){
						$update=mysqli_query($connection,"UPDATE Chiavi SET gcmRegistration='{$token}' WHERE id_key='{$query['id_key']}'");
					
						$image=mysqli_query($connection,"SELECT * FROM Image Where id_user='{$query['id_user']}'");
						if($image){
							
							$update=mysqli_query($connection,"UPDATE User SET created_at = null WHERE id_user='{$query['id_user']}'");
							$image=mysqli_fetch_assoc($image);
							$response["errore"]=false;
							$response["abilitato"]=true;
							$response["id_user"]=$query["id_user"];
							$response["name"]=$query["name"];
							$response["email"]=$query["email"];
							$response["ultimoAccesso"]=$query["created_at"];
					
							$aes = new AES();
							$aes->setKey("caputotavellamantovani99");
							$aes->setBlockSize(256);
						
							$aes->setData($pass["publicKey"]);
							$response["publicKey"]=$aes->decrypt();
						
							$aes->setData($pass["privateKey"]);
							$response["privateKey"]=$aes->decrypt();
						
							$aes->setData($token);
						
							$response["gcm"]=$aes->decrypt();
						
							$response["urlImage"]=$image["url"];
							$response["idImage"]=$image["id_image"];
					}else{
						$response["errore"]=true;
						$response["risultato"]="errore image";
						$response["abilitato"]=false;
					}
				/*}else{
					$response["errore"]=true;
					$response["risultato"]="login";
					$response["abilitato"]=false;
					
				}*/
			}else{
					$response["errore"]=true;
					$response["abilitato"]=false;
					$response["risultato"]="password Errata";
						
				}
			}else{
				$response["errore"]=true;
				$response["risultato"]="errore password";
			}
			
		}else{
			
			$response["errore"]=true;
			$response["risultato"]="errore utenti";
		}
	} else{
		$response["errore"]=true;
		$response["risultato"]="errore connessione";
	}
	echo json_encode($response);	
	
}


?>