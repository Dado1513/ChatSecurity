<?php

class DbConnect{
	private $conn;
	function __construct(){
		
	}
	
	function connect(){
		//percorso assoluto dove risiede il file, cosi che se vogliamo includere in piu file non avremmo error
		include_once dirname(__FILE__).'/config.php';
		//ci connettiamo al database
		$this->conn=new mysqli(DB_HOST,DB_USERNAME,DB_PASSWORD,DB_NAME);
		
		if(mysqli_connect_errno()){
			echo "Connessione fallita". mysqli_connect_error();
		}
		
		return $this->conn;
		
	}
	
}
?>