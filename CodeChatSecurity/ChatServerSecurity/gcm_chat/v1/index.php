<?php

error_reporting(-1);
ini_set('display_errors', 'On');

require_once '../include/db_handler.php';
require '.././libs/Slim/Slim.php';

\Slim\Slim::registerAutoloader();
 
$app = new \Slim\Slim();
 
// User login 
$app->post('/user/login', function() use ($app) {
    // check for required params
    verifyRequiredParams(array('name', 'email'));
 
    // reading post params
    $name = $app->request->post('name');
    $email = $app->request->post('email');
 
    // validating email address
    validateEmail($email);
 
    $db = new DbHandler();
    $response = $db->createUser($name, $email);
 
    // echo json response
    echoRespnse(200, $response);
});


//aggiornamento User usiamo questa url per aggiornare gcm registration id
$app->put('user/:id',function($user_id) use($app){
	global $app;
	verifyRequiredParams($array('gcm_registration_id'));
	
	$gcm_registration_id=$app->request->put('gcm_registration_id');
	$db=new DbHandler();
	$response=$db->updateGcmID($user_id,$gcm_registration_id);
	echoRespnse(200,$response);
});



//tutte le chat room

$app->get('/chat_rooms',function(){
	$response=array();
	$db=new DbHandler();
	
	$result=$db->getAllChatrooms();
	
	$response["errore"]=false;
	
	$response["chat_rooms"]=array();
	
	while($chat_room=$result->fetch_assoc()){
		$tmp=array();
		$tmp["chat_room_id"] = $chat_room["chat_room_id"];
        $tmp["name"] = $chat_room["name"];
        $tmp["created_at"] = $chat_room["created_at"];
        array_push($response["chat_rooms"], $tmp);
		
	}
	echoRespnse(200,$response);
	
});

//messaggio in una chatroom mandere una notifica
$app->post('/chat_rooms/:id/message',function ($chat_room_id){
	global $app;
	$db=new DbHandler();
	
	verifyRequiredParams(array('user_id','message'));
	
	$user_id=$app->request->post('user_id');
	$message=$app->request->post('message');
	
	$response=$db->addMessage($user_id,$chat_room_id,$message);
	
	//nessun errore
	if($response['errore']==false){
		require_once __DIR__ .'/../libs/gcm/gcm.php';
		require_once __DIR__ .'/../libs/gcm/push.php';
		$gcm=new GCM();
		$push=new Push();
		
		//otteniamo l'user usando user_id
		$user=$db->getUser($user_id);
		
		$data=array();
		$data['user']=$user;
		$data['message']=$response['message'];
		$data['chat_room_id']=$chat_room_id;
		
		$push->setTile("Google Cloud Messaging");
		$push->setIsBackground(FALSE);
		$push->setFlag(PUSH_FLAG_CHATROOM);
		$push->setData($data);
		
		//mando il messaggio push nel topic
		$gcm->sendToTopic('topic_' . $chat_room_id,$push->getPush());
		
		$response['user']=$user;
		$response['errore']=false;
	}
	echoRespnse(200,$response);
	
});

//mando la notifica push ad un sigolo utente
$app->post('user/:id/message', function($to_user_id){
	global $app;
	$db=new DbHandler();
	 verifyRequiredParams(array('message'));
	 
	 $from_user_id=$app->request->post('user_id');
	 $message=$app->request->post('message');
	 
	 $response=$db->addMessage($from_user_id,$to_user_id,$message);
	 
	 if($response['errore']==false){ 
		require_once __DIR__ . '/../libs/gcm/gcm.php';
        require_once __DIR__ . '/../libs/gcm/push.php';
        $gcm = new GCM();
        $push = new Push();
		
		$user=$db->getUser($to_user_id);
		
		$data=array();
		
		$data['user']=$user;
		$data['message']=$response['message'];
		//volendo le immagin
		$data['image']='';
	 
		$push->setTitle("Google Cloud Messaging");
        $push->setIsBackground(FALSE);
        $push->setFlag(PUSH_FLAG_USER);
        $push->setData($data);
 
        // sending push message to single user
        $gcm->send($user['gcm_registration_id'], $push->getPush());
 
        $response['user'] = $user;
        $response['error'] = false;
    }
 
    echoRespnse(200, $response);
});

//mandare la notifca a 1000 utentu in contemporanea
$app->post('/user/message',function () use ($app){
	
	$response=array();
	verifyRequiredParams(array('user_id','to','message'));
	
	require_once __DIR__ .'/../libs/gcm/gcm.php';
	require_once __DIR__ .'/../libs/gcm/push.php';
	
	$db=new DbHandler();
	
	//prendiamo i campi che si voglioni inviare
	//id mittente,id destinatari, messaggio
	$user_id = $app->request->post('user_id');
    $to_user_ids = array_filter(explode(',', $app->request->post('to')));
    $message = $app->request->post('message');
 
	$user=$db->getUser($user_id);
	$users=$db->getUsers($to_user_ids);
	
	$registration_ids=array();
	//recupero id di registrazione
	foreach($user as $u){
		
		array_push($registration_ids,$u['gcm_registration_id']);
	}
	
	//inserisco il messaggio nel db e lo mando a tutti
	$gcm=new GCM();
	$push=new Push();
	
	$msg=array();
	$msg['message']=$message;
	$msg['message_id']='';
	$msg['chat_room_id']='';
	$msg['created_at']=date('Y-m-d G:i:s');
	
	//costruisco i dati da inviare
	$data = array();
    $data['user'] = $user;
    $data['message'] = $msg;
    $data['image'] = '';
 
    $push->setTitle("Google Cloud Messaging");
    $push->setIsBackground(FALSE);
    $push->setFlag(PUSH_FLAG_USER);
    $push->setData($data);
	
	$gcm->sendMultiple($registration_ids,$push->getPush());
	
	$response["errore"]=false;
	echoRespnse(200,$response);
	
	
	
	
});

$app->post('/users/send_to_all', function() use ($app) {
 
    $response = array();
    verifyRequiredParams(array('user_id', 'message'));
 
    require_once __DIR__ . '/../libs/gcm/gcm.php';
    require_once __DIR__ . '/../libs/gcm/push.php';
 
    $db = new DbHandler();
 
    $user_id = $app->request->post('user_id');
    $message = $app->request->post('message');
 
    require_once __DIR__ . '/../libs/gcm/gcm.php';
    require_once __DIR__ . '/../libs/gcm/push.php';
    $gcm = new GCM();
    $push = new Push();
 
    // get the user using userid
    $user = $db->getUser($user_id);
     
    // creating tmp message, skipping database insertion
    $msg = array();
    $msg['message'] = $message;
    $msg['message_id'] = '';
    $msg['chat_room_id'] = '';
    $msg['created_at'] = date('Y-m-d G:i:s');
 
    $data = array();
    $data['user'] = $user;
    $data['message'] = $msg;
    $data['image'] = 'http://www.androidhive.info/wp-content/uploads/2016/01/Air-1.png';
 
    $push->setTitle("Google Cloud Messaging");
    $push->setIsBackground(FALSE);
    $push->setFlag(PUSH_FLAG_USER);
    $push->setData($data);
 
    // sending message to topic `global`
    // On the device every user should subscribe to `global` topic
    $gcm->sendToTopic('global', $push->getPush());
 
    $response['user'] = $user;
    $response['error'] = false;
 
    echoRespnse(200, $response);
});
 
/**
 * Fetching single chat room including all the chat messages
 *  */
$app->get('/chat_rooms/:id', function($chat_room_id) {
    global $app;
    $db = new DbHandler();
 
    $result = $db->getChatRoom($chat_room_id);
 
    $response["error"] = false;
    $response["messages"] = array();
    $response['chat_room'] = array();
 
    $i = 0;
    // looping through result and preparing tasks array
    while ($chat_room = $result->fetch_assoc()) {
        // adding chat room node
        if ($i == 0) {
            $tmp = array();
            $tmp["chat_room_id"] = $chat_room["chat_room_id"];
            $tmp["name"] = $chat_room["name"];
            $tmp["created_at"] = $chat_room["chat_room_created_at"];
            $response['chat_room'] = $tmp;
        }
 
        if ($chat_room['user_id'] != NULL) {
            // message node
            $cmt = array();
            $cmt["message"] = $chat_room["message"];
            $cmt["message_id"] = $chat_room["message_id"];
            $cmt["created_at"] = $chat_room["created_at"];
 
            // user node
            $user = array();
            $user['user_id'] = $chat_room['user_id'];
            $user['username'] = $chat_room['username'];
            $cmt['user'] = $user;
 
            array_push($response["messages"], $cmt);
        }
    }
 
    echoRespnse(200, $response);
});




//verifica dei parametri
function verifyRequiredParams($required_fields) {
    $error = false;
    $error_fields = "";
    $request_params = array();
    $request_params = $_REQUEST;
    // Handling PUT request params
    if ($_SERVER['REQUEST_METHOD'] == 'PUT') {
        $app = \Slim\Slim::getInstance();
        parse_str($app->request()->getBody(), $request_params);
    }
    foreach ($required_fields as $field) {
        if (!isset($request_params[$field]) || strlen(trim($request_params[$field])) <= 0) {
            $error = true;
            $error_fields .= $field . ', ';
        }
    }
 
    if ($error) {
        // Required field(s) are missing or empty
        // echo error json and stop the app
        $response = array();
        $app = \Slim\Slim::getInstance();
        $response["error"] = true;
        $response["message"] = 'Required field(s) ' . substr($error_fields, 0, -2) . ' is missing or empty';
        echoRespnse(400, $response);
        $app->stop();
    }
}

/**
 * Validating email address
 */
function validateEmail($email) {
    $app = \Slim\Slim::getInstance();
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $response["error"] = true;
        $response["message"] = 'Email address is not valid';
        echoRespnse(400, $response);
        $app->stop();
    }
}
 
function IsNullOrEmptyString($str) {
    return (!isset($str) || trim($str) === '');
}
 
/**
 * Echoing json response to client
 * @param String $status_code Http response code
 * @param Int $response Json response
 */
function echoRespnse($status_code, $response) {
    $app = \Slim\Slim::getInstance();
    // Http response code
    $app->status($status_code);
 
    // setting response content type to json
    $app->contentType('application/json');
 
    echo json_encode($response);
}
 
$app->run();
?>