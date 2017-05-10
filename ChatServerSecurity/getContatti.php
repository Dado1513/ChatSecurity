<?php

	include_once('include/config.php');	
	include_once('include/AES.php');
	if(isset($_POST['root']) && $_POST['root']=="caputotavellamantovani99" && isset($_POST["id"]) && isset($_POST["password"])){
		$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME) or die( "Unable to connect");
		
		$id=$_POST["id"];
		$id=mysqli_real_escape_string($connection,$id);
		$password=$_POST["password"];
		$password=mysqli_real_escape_string($connection,$password);
		$result1=mysqli_query($connection,"SELECT * FROM User WHERE id_user='{$id}'");
		if($result1){
			$result1=mysqli_fetch_assoc($result1);
			$result2=mysqli_query($connection,"SELECT * FROM Chiavi WHERE id_key ='{$result1['id_key']}' AND password='{$password}'");
			if($result2){
				$aes = new AES();
				$aes->setKey("caputotavellamantovani99");
				$aes->setBlockSize(256);
				if(!mysqli_connect_errno()){
					$q = mysqli_query($connection,"SELECT * from User INNER JOIN Chiavi   
							ON User.id_key = Chiavi.id_key;  ") or die("errore");
					$response=array();
					$response["errore"]=false;
					$response["user"]=array();
			
					while ($user = mysqli_fetch_assoc($q)) {
						//aggiungere se non si vuole mandare mex a chi è non loggato
						
						$tmp = array();
						$tmp["user_id"] = $user["id_user"];
						$tmp["username"] = $user["name"];
						$tmp["statoPersonale"]=$user["Stato_Personale"];
					
						$tmp["email"]=$user["email"];
						$image=mysqli_query($connection,"SELECT * FROM Image Where id_user='{$tmp['user_id']}'");
						$image=mysqli_fetch_assoc($image);
						$tmp["id_immagine"]=$image["id_image"];
						$tmp["urlImage"]=$image["url"];
		
						$aes->setData($user["publicKey"]);
						$dec=$aes->decrypt();
						$tmp["publicKey"] = $dec;
				
						array_push($response["user"], $tmp);
						
					}

				echo json_encode($response);
				
				mysqli_close($connection);
				exit;
				}
				$response=array();
				$response["errore"]=true;
				echo json_encode($response);
			}else{
				$response=array();
				$response["errore"]=true;
				echo json_encode($response);
		
			}
		}else{
			$response=array();
			$response["errore"]=true;
			echo json_encode($response);
		}
	}
	
?>