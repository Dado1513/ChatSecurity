<?php
	
	require_once '../include/db_handler.php';
	$name=$_POST['name'];
	$email=$_POST['email'];
	//validateEmail($email);

    $db = new DbHandler();
    $response = $db->createUser($name, $email);
 
    // echo json response
    echoRespnse(200, $response);
	

	function validateEmail($email) {
		if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
			$response["errore"] = true;
			$response["message"] = 'Email address is not valid';
			echoRespnse(400, $response);
		}
	}
 
	function IsNullOrEmptyString($str) {
		return (!isset($str) || trim($str) === '');
	}
 
	function echoRespnse($status_code, $response) {
		
		print( json_encode($response));
	}
	
?>

