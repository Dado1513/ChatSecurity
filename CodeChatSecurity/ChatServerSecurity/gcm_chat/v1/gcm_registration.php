<?php


    require_once '../include/db_handler.php';
    if(isset($_POST["id"]) && isset($_POST["gcm_registration_id"])){
        $id=$_POST["id"];
        $gcm_id=$_POST["gcm_registration_id"];
        $db=new DbHandler();
        $response=$db->updateGcmID($id,$gcm_id);
        echoRespnse(200,$response);
        
        
    }
    function echoRespnse($status_code, $response) {
        echo json_encode($response);
    }   
?>


