<?php
	
	require_once '../include/db_handler.php';
	$db = new DbHandler();

	$chat_rooms_id=$_POST["chat_rooms_id"];
	
    $result = $db->getChatRoom($chat_rooms_id);

    $response["errore"] = false;
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

    echo json_encode($response);

?>