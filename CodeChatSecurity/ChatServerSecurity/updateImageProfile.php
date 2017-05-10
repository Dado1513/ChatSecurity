<?php


include_once('include/config.php');	
//file per cifrare i dati	
include_once('include/AES.php');
$response=array();

if(isset($_POST['filename']) &&isset($_POST['id']) && isset($_POST['email']) 
			&& isset($_POST['password'])&& isset($_POST['image']) && isset($_POST["root"]) && $_POST["root"]=="caputotavellamantovani99" ){

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
					$base=$_POST['image'];
					$filename=$_POST['filename'];
					$array=explode(".",$filename);

                    if(isset($array[1]))
                        $filename=$query["id_user"].".".
                            $array[1];

                    else
					    $filename=$query["id_user"].".jpg";
					$binary=base64_decode($base);
					header('Content-Type: bitmap; charset=utf-8');
					$file = fopen('uploadedimages/'.$filename, 'wb');
					$name="uploadedimages/".$filename;
					fwrite($file, $binary);
					fclose($file);
					$result=mysqli_query($connection,"SELECT * FROM Image WHERE id_user='{$query['id_user']}'");
					if($result){
						$result=mysqli_fetch_assoc($result);
						$result3=mysqli_query($connection,"UPDATE Image SET url = '{$name}' WHERE id_image = '{$result["id_image"]}'");
						if($result3){
							$response["errore"]=false;
						}else{
							$response["errore"]=true;
							$response["risultato"]="errore aggiornamento url";
						}

					}else{
						$response["errore"]=true;
						$response["risultato"]="errore selezionament imamagine";
					}
					

				}else{
					$response["errore"]=true;
					$response["risultato"]="password non corretta";
				}
			}else{
				$response["errore"]=true;
				$response["risultato"]="query chiave non esatta";
				
			}
		}else{
			$response["errore"]=true;
			$response["risultato"]="query username non corretta";
				
		}
	}else{
		$response["errore"]=true;
		$response["risultato"]="connessione db non funzionante";
				
	}

	echo json_encode($response);


}



?>