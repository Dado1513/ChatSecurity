<?php

include_once('include/config.php');	
//file per cifrare i dati	
include_once('include/AES.php');
$response=array();

if(isset($_POST['filename']) &&isset($_POST['username']) && isset($_POST['email']) 
			&& isset($_POST['password'])&& isset($_POST['image'])&& isset($_POST['publicKey']) 
			&& isset($_POST['privateKey']) && isset($_POST['gcm'])){

	$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME) or die( "Unable to connect");
	if(!mysqli_connect_errno()){
		//se sono qui mi sono connesso senza errori
	
		//inserisco prima le chiavi cifrate
		//cosi ottengo l'identificativo delle chiavi checkdate
		//andro ad inserire nella tabella utenti
		$password=mysqli_real_escape_string($connection,$_POST['password']);
		$username=mysqli_real_escape_string($connection,$_POST['username']);
		$email=mysqli_real_escape_string($connection,$_POST['email']);
		
		//**inizio la cifratura dei dati sensibili**//
		$publicKey=$_POST['publicKey'];
		$privateKey=$_POST['privateKey'];
		$gcm=$_POST['gcm'];
		//cifro la password con md5 prima di inserirla nel db
		$password=md5($password);
		//chiavePubblica
		$blockSize = 256;
		
		$aes = new AES($publicKey,"caputotavellamantovani99", $blockSize);
		
		$publicKeyEncrypt = $aes->encrypt();
		//***passi per la decifrazione**//
		//$aes->setData($enc);
		//$dec=$aes->decrypt();
		//chiavePrivata
		$aes1 = new AES($privateKey,"caputotavellamantovani99", $blockSize);
		$privateKeyEncrypt=$aes1->encrypt();
	
	
		$aes2=new AES($gcm,"caputotavellamantovani99",$blockSize);
		$gcmEncrypt=$aes2->encrypt();
		
		$result=mysqli_query($connection,"INSERT INTO Chiavi VALUES (NULL, '{$password}', '{$publicKeyEncrypt}', '{$privateKeyEncrypt}', '{$gcmEncrypt}')") ;
		$id_key=mysqli_insert_id($connection);
		if($result){
			//echo mysqli_error($connection);
			
			$result2=mysqli_query($connection,"INSERT INTO User (id_user,name,email,image,id_key)  VALUES  (null,'{$username}','{$email}','1','{$id_key}')");
			if($result2){
				$response['errore']=false;
				$response['id']=mysqli_insert_id($connection);
				$response['username']=$username;
				$response['email']=$email;
				$response['password']=$password;
				//stringa contentente l'iimmagine
				$base=$_POST['image'];
				$filename=$_POST['filename'];
				$array=explode(".",$filename);
				$filename=$response['id'].".".$array[1];
				$binary=base64_decode($base);
				header('Content-Type: bitmap; charset=utf-8');
				$file = fopen('uploadedimages/'.$filename, 'wb');
				$name="uploadedimages/".$filename;
				fwrite($file, $binary);
				fclose($file);
				
				$result3=mysqli_query($connection,"INSERT INTO Image VALUES (null,'{$response['id']}','{$name}')");
				if($result3){
					$response["id_image"]=mysqli_insert_id($connection);
					$response["urlImage"]=$name;
				} else{
					$response["errore"]=true;
					$response["risultato"]="impossibile inserire immagine";
				}
			}else{
				$response['errore']=true;
				$response['risultato']="errore inserimento utente ->".mysqli_error($connection);
				mysqli_query($connection,"DELETE FROM Chiavi WHERE id_key='{$id_key}'");
			}
			//print_r($response);
		}else{
			$response['errore']=true;
			$response['risultato']="errore inserimento chiavi".mysqli_error($connection);
		}
	/*
	//per salvare le immagini
		//save image
		$base=$_POST['image'];
		// Get file name posted from Android App
		$filename = $_POST['filename'];
		// Decode Image
		$binary=base64_decode($base);
    
		header('Content-Type: bitmap; charset=utf-8');
		
		// devo trovare un nome per l'immagine
	
		mysqli_query("INSERT INTO users ") or die("non va la q");

		
		
		
		// Images will be saved under 'www/imgupload/uplodedimages' folder
		$file = fopen('uploadedimages/'.$filename, 'wb');
		// Create File
		fwrite($file, $binary);
		fclose($file);*/
	}else{
		$response['errore']=true;
		$response['risultato']="error parametri connessione database";
	
	}
}else{
	$response['errore']=true;
	$response['risultato']="error parametri non settati";
}


	print json_encode($response);

?>