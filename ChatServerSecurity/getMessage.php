<?php


	include_once('include/config.php');
	include_once('include/AES.php');

	if(isset($_POST['root']) && isset($_POST['id']) && $_POST['root']=="caputotavellamantovani99"&& isset($_POST["password"]) ){
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

				if(!mysqli_connect_errno()){
					$id=mysqli_real_escape_string($connection,$_POST['id']);
					$result=mysqli_query($connection,"SELECT * FROM Have WHERE id_user='{$id}'");
					$response=array();
					if($result){
						$response["messaggi"]=array();
						while($have=mysqli_fetch_assoc($result)){
							//ottengo tutti i messaggi e li inserisco
							$message=mysqli_query($connection,"SELECT * FROM Message WHERE id_conversation='{$have['id_conversation']}'");
							if($message){
								while($text=mysqli_fetch_assoc($message)){
									$messaggio=array();
									$messaggio["id_message"]=$text["id_message"];
									if($text["id_user"]!=$id)
										$messaggio["text"]=$text["text"];
									else{
										$messageForMe=mysqli_query($connection,"SELECT * FROM MessageForMe WHERE id_message='{$text['id_message']}'");
										$messageForMe=mysqli_fetch_assoc($messageForMe);
										$messaggio["text"]=$messageForMe["text"];
									}
									$messaggio["id_conversation"]=$text["id_conversation"];
									$messaggio["id_user"]=$text["id_user"];
									$messaggio["created_at"]=$text["createt_at"];
									if($text["is_image"]=="0")
										$messaggio["is_image"]="0";
									else
										$messaggio["is_image"]="1";

									array_push($response["messaggi"], $messaggio);
								}
							}else{
								$response["messagioErrore"]=true;
								$response["risultato"]="errore nella query Message";
							}
						}$response["errore"]=false;
					}else{
						$response["errore"]=true;
						$response["risultato"]="errore nella query have";
					}
				}else{
					$response["errore"]=true;
					$response["risultato"]="errore nella connessione";
				}echo
				json_encode($response);
			}else{
				$response["errore"]=true;
				$response["risultato"]="errore utente non esistnte,p";
				echo json_encode($response);
			}
		}else{

			$response["errore"]=true;
			$response["risultato"]="errore utente non esistnte";
			echo json_encode($response);
		}
	}else{

	}



?>
