<?php 
//classe per tutte le operazioni del database
class DbHandler{
	
	private $conn;
	function __construct(){
		require_once dirname(__FILE__).'/dbconnect.php';
		$db=new DbConnect();
		$this->conn=$db->connect();
	
	}
	
	public function createUser($name,$email){
		$response=array();
		//primo controllo se utente esiste gia nel db
		if(!$this->isUserExist($email)){
			//prepare staement
			$result=$this->conn->query("INSERT INTO users(name,email) values ('$name','$email')");
			//passi i parametri $name,$email nella stringa preparata prima
			//$stmt->bind_param("ss",$name,$email);
			//$result=$stmt->execute();
			//stmt->close();
			
			
			if($result){
				//user inserito
				$response["errore"]=false;
				//$response["user"]=$this->getUserByEmail($email);
                $user=$this->getUserByEmail($email);
                $response["user_id"]=$user["user_id"];
                $response["name"]=$user["name"];
                $response["email"]=$user["email"];
                $response["created_at"]=$user["created_at"];
			} else {
				//inserimento fallito
				echo "Error: " . "<br>" . $this->conn->error;
				$response["errore"]=true;
				$response["user"]="Un errore e' avvenuto";
			}
		} else{
			//utente con la stessa email gia esistente
			$response["errore"]=false;
			$user=$this-> getUserByEmail($email);
            $response["user_id"]=$user["user_id"];
            $response["name"]=$user["name"];
            $response["email"]=$user["email"];
            $response["created_at"]=$user["created_at"];
		    
        }
		
		return $response;
	}
	
	
	//aggiornamento dell'ID GCM
	
	public function updateGcmID($user_id,$gcm_registration_id){
		
		$response=array();
		$stmt=$this->conn->prepare("UPDATE users SET gcm_registration_id=? where user_id=?");
		$stmt->bind_param("si",$gcm_registration_id,$user_id);
		if($stmt->execute()){
			
			$response["errore"]=false;
			$response["message"]="GCM REGISTRATION ID UPDATE SUCCESS";
		} else {
			
			$response["errore"]=true;
			$response["message"]="FAILED UPDATE GCM ID";
			$stmt->error();
		}
		
		$stmt->close();
		return $response;
	
	}
	
	public function getUser($user_id){
		$stmt=$this->conn->query("SELECT user_id,name,email,gcm_registration_id,created_at FROM users WHERE user_id='{$user_id}'");
		
		//esegue la query
		if($stmt){
			//prepare le variabili come risulatto della query
			$result=$stmt->fetch_assoc();
			//$stmt->bind_result($user_id,$name,$email,$gcm_registration_id,$created_at);
			//$stmt->fetc();
			$user=array();
			
			$user["user_id"]=$result['user_id'];
			$user["name"]=$result['name'];
			$user["email"]=$result['email'];
			$user["gcm_registration_id"]=$result['gcm_registration_id'];
			$user["created_at"]=$result['created_at'];
			return $user;
			
		} else {
			return NULL;
		}
	}
	
	public function getUsers($user_ids){
		$users=array();
		if(sizeof($user_ids) > 0 ){
			$query="SELECT user_id,name,email,gcm_registration_id,created_at FROM users WHERE user_id IN(";
			foreach($user_ids as $user_id){
				$query .= $user_id. ',';
			}
			//tolgo l'ultima virgola
			$query=substr($query,0,strlen($query)-1);
			$query.=')';
			
			$stmt=$this->conn->prepare($query);
			$stmt->execute();
			$result=$stmt->get_result();
			while ($user = $result->fetch_assoc()) {
					$tmp = array();
					$tmp["user_id"] = $user['user_id'];
					$tmp["name"] = $user['name'];
					$tmp["email"] = $user['email'];
					$tmp["gcm_registration_id"] = $user['gcm_registration_id'];
					$tmp["created_at"] = $user['created_at'];
					//inseriesce uno o piu elemnti in questo caso un array associativo alla fine di un array
					//come fosse uno stack
					array_push($users, $tmp);
				}
			}
		
		return $users;
			
		}
	

	public function addMessage ($user_id,$chat_room,$message){
		$response=array();
		
		$stmt = $this->conn->query("INSERT INTO messages (message_id,chat_room_id, user_id, message) values(null,'$chat_room','$user_id' ,'$message' )");
        //$stmt->bind_param("iis", $chat_room_id, $user_id, $message);
		
		//$result= $stmt->execute();
		if($stmt){
			$response["errore"]=false;
			$message_id=$this->conn->insert_id;
			$stmt=$this->conn->query("SELECT message_id, user_id, chat_room_id, message, created_at FROM messages WHERE message_id = '{$message_id}'");
			//$stmt->bind_param("i",$message_id);
			if($stmt){
				//$stmt->bind_result($message_id,$user_id,$chat_room_id,$message,$created_at);
				//associa le variabili preparate sopra
				//$stmt->fetc();
				$result=$stmt->fetch_assoc();
				$tmp=array();
				$tmp["message_id"]=$result['message_id'];
				$tmp["chat_room_id"]=$result['chat_room_id'];
				$tmp["message"]=$result['message'];
				$tmp['created_at']=$result['created_at'];
				$response["message"]=$tmp;
				
			}
		} else{
			$response["errore"]=true;
			$response["message"]='FAILED MESSAGE SENT'. $this->conn->error;
		}
	
		return $response;
	}
	
	//tutte le chat
	public function getAllChatrooms(){
		$stmt = $this->conn->query("SELECT * FROM chat_rooms");
        //$stmt->execute();
		
        //$tasks = $stmt->get_result();
        //$stmt->close();
        return $stmt;
    }
	
	function getChatRoom($chat_room_id){
		$stmt = $this->conn->query("SELECT cr.chat_room_id, cr.name, cr.created_at as chat_room_created_at, u.name as username, c.* FROM chat_rooms cr LEFT JOIN messages c ON c.chat_room_id = cr.chat_room_id LEFT JOIN users u ON u.user_id = c.user_id WHERE cr.chat_room_id = '{$chat_room_id}'");
        //$stmt->bind_param("i", $chat_room_id);
        //$stmt->execute();
        //$tasks = $stmt->get_result();
        //$stmt->close();
        return $stmt;
	}

	//controllo duplicati email
	private function isUserExist($email) {
        $stmt = $this->conn->prepare("SELECT user_id from users WHERE email = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }
	
	
	//otteniamo l'utente dall'email
	public function getUserByEmail($email) {
        $stmt = $this->conn->prepare("SELECT user_id, name, email, created_at FROM users WHERE email = ?");
        $stmt->bind_param("s", $email);
        if ($stmt->execute()) {
            // $user = $stmt->get_result()->fetch_assoc();
            $stmt->bind_result($user_id, $name, $email, $created_at);
            $stmt->fetch();
            $user = array();
            $user["user_id"] = $user_id;
            $user["name"] = $name;
            $user["email"] = $email;
            $user["created_at"] = $created_at;
            $stmt->close();
            return $user;
        } else {
            return NULL;
        }
    }
 
	
	
}



?>