<?php
class GCM{


	function __construct(){
		
	}
	
	//mandare un messaggio ad un singolo utente
	public function send($to,$message){
		$fields=array(
		'to'=>$to,
		'data'=>$message,
		);
		return $this->sendPushNotification($fields);
	}
	//mandare un messaggio in un topic
	public function sendToTopic($to,$message){
		$fields=array(
		'to'=>'/topics/'.$to,
		'data'=>$message,
		);
		return $this->sendPushNotification($fields);
	}
	
	
	//mandare un messaggio multiplo
	public function sendMultiple($registration_ids,$message){
		$fields=array(
		'registration_ids'=>$registration_ids,
		'data'=>$message,
		);
		return $this->sendPushNotification($fields);
	}
	
	//funzione curl richiest aal server gcm
	private function sendPushNotification($fields){
		
		include_once __DIR__ .'/../../include/config.php';
		$url = 'https://gcm-http.googleapis.com/gcm/send';
		
		$headers=array(
		'Authorization: key=' .GOOGLE_API_KEY,
        'Content-Type: application/json'
        );
		//open connection
		$ch=curl_init();
		
		//impostazione dell'url, variabili via post e datiinviati via post
		curl_setopt($ch,CURLOPT_URL,$url);
		
		//inio dei dati tramite il post
		curl_setopt($ch,CURLOPT_POST,true);
		//imposto gli header
		curl_setopt($ch,CURLOPT_HTTPHEADER,$headers);
		//evito che il contenuto remoto venga passato sotto forma di print
		curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
		curl_setopt( $ch, CURLOPT_IPRESOLVE, CURL_IPRESOLVE_V4);
		//disattivazione dei cerificati supportati temporaneamente
		curl_setopt($ch,CURLOPT_SSL_VERIFYPEER,true);
		curl_setopt($ch,CURLOPT_POSTFIELDS,json_encode($fields));
		
		//esecuxione della curl
		
		//salvo il risultato in una variabile
		$result=curl_exec($ch);
		if($result==false){
			die('CURL FAILED'.curl_error($ch)."".$headers);
		}
		curl_close($ch);
		
		return $result;
	}
	
}

?>