<?php


    $message = $_POST['message'];
    $apiKey =$_POST['api_key'];
    $token = $_POST['token'];
    $image = $_POST['include_image'];

    $data = array();
    $data['title'] = 'Chatti';
    $data['message'] = $message;
    if ($image == 'true') {
        $data['image'] = 'http://api.androidhive.info/gcm/panda.jpg';
    } else {
        $data['image'] = '';
    }
	
    $data['created_at'] = date('Y-m-d G:i:s');

    $fields = array(
        'to' => $token,
        'data' => $data,
    );

    // Set POST variables
    $url = 'https://gcm-http.googleapis.com/gcm/send';

    $headers = array(
        'Authorization: key=' . $apiKey,
        'Content-Type: application/json'
    );
    // Open connection
    $ch = curl_init();

    // Set the url, number of POST vars, POST data
    curl_setopt($ch, CURLOPT_URL, $url);

    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    // Disabling SSL Certificate support temporarly
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);

    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

    $response = array();

    // Execute post
    $result = curl_exec($ch);
    if ($result === FALSE) {
        $response['error'] = TRUE;
        $response['message'] = 'Unable to send test push notification';
        echoRespnse(200, $response);
        exit;
    }

    // Close connection
    curl_close($ch);

    $response['error'] = FALSE;
    $response['message'] = 'Test push message sent successfully!';

//    echoRespnse(200, $response);

	print($response['error']);
	?>