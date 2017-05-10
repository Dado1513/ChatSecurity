<?php
	require_once '../include/db_handler.php';
	$db= new DbHandler();

	
	$result= $db->getAllChatrooms();
	$response=array();
	$response["errore"]=false;
	$response["chat_rooms"]=array();
	
	while ($chat_room = $result->fetch_assoc()) {
        $tmp = array();
        $tmp["chat_room_id"] = $chat_room["chat_room_id"];
        $tmp["name"] = $chat_room["name"];
        $tmp["created_at"] = $chat_room["created_at"];
        array_push($response["chat_rooms"], $tmp);
    }

    echo json_encode($response);

?>