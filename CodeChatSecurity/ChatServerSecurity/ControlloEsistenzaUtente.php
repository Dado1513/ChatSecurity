<?php
$response=array();
if(isset($_POST['username']) && isset($_POST['email']) ){
	
include_once('include/config.php');
		
	$email = $_POST['email'];
	$username = $_POST['username'];
	

	
	// devo trovare un nome per l'immagine
	$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME);
	if(!mysqli_connect_errno()){
		$email = mysqli_real_escape_string($connection,$email);
		$username = mysqli_real_escape_string($connection,$username);
	
	
	
		// controllo quante righe tornano con l'username inserito dall'utente
		$result = mysqli_query($connection,"SELECT * FROM User WHERE name ='{$username}'") or die("non va la query userbane");
		$righeUsername = mysqli_num_rows($result);
		//$righeUsername=0;
		// controllo quante righe tornano con l'email inserito dall'utente
		
		$u = mysqli_query($connection,"SELECT * FROM User WHERE email='{$email}'") or die("non va la query email");
		$righeEmail = mysqli_num_rows($u);
		
			// 0 vanno male entrambo
		if($righeUsername>0 && $righeEmail>0 ){
			$response['errore']=false;
			$response['risultato']=0;
			
			}else if( $righeEmail>0 ){
				// 1 va male solo l'email
				$response['errore']=false;
				$response['risultato']=1;
			
			}else if( $righeUsername>0 ){
				// 2 se va male l'username 
				$response['errore']=false;
				$response['risultato']=2;
			
			}else {
				$response['errore']=false;
				$response['risultato']=3;
			}
			
	}else {
		$response['errore']=true;
		$response['risultato']="non si connette";
			
	}
	echo json_encode($response);	 
		
}else{
	$response['errore']=true;
	$response['risultato']="parametri non settati";
	echo json_encode($response);
	} 
	
	
		
	
?>