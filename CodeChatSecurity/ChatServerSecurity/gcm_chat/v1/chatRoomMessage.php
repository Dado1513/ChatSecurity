<?php
	
	require_once '../include/db_handler.php';
	
	$db=new DbHandler();
	$user_id=$_POST["user_id"];
	$message=$_POST["message"];
	$chatRoomId=$_POST["chatRoomId"];
	
	$response=$db->addMessage($user_id,$chatRoomId,$message);
	if ($response['errore'] == false) {
        require_once __DIR__ . '/../libs/gcm/gcm.php';
        require_once __DIR__ . '/../libs/gcm/push.php';
        $gcm = new GCM();
        $push = new Push();

        // get the user using userid
        $user = $db->getUser($user_id);

        $data = array();
        $data['user'] = $user;
        $data['message'] = $response['message'];
        $data['chatRoomId'] = $chatRoomId;

        $push->setTitle("Google Cloud Messaging");
        $push->setIsBackground(FALSE);
        $push->setFlag(PUSH_FLAG_CHATROOM);
        $push->setData($data);

        // echo json_encode($push->getPush());exit;
        // sending push message to a topic
        $gcm->sendToTopic('topic_' . $chatRoomId, $push->getPush());

        $response['user'] = $user;
        $response['errore'] = false;
    }
	echo json_encode($response);


?>