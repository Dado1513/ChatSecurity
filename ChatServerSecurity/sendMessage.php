<?php
	include_once('include/config.php');
	include_once('include/AES.php');
	$response=array();
	if(isset($_POST['root']) && isset($_POST['id']) && $_POST['root']=="caputotavellamantovani99"&& isset($_POST["password"]) && isset($_POST["id_conversazione"]) && isset($_POST["textOther"]) && isset($_POST["textMy"]) && isset($_POST["idOther"]) ) {
		$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME) or die( "Unable to connect");
		if(!mysqli_connect_errno()){

			$id=$_POST["id"];
			$id=mysqli_real_escape_string($connection,$id);
			$password=$_POST["password"];
			$password=mysqli_real_escape_string($connection,$password);
			$result1=mysqli_query($connection,"SELECT * FROM User WHERE id_user='{$id}'");
			if($result1){
				$result1=mysqli_fetch_assoc($result1);
				$result2=mysqli_query($connection,"SELECT * FROM Chiavi WHERE id_key ='{$result1['id_key']}' AND password='{$password}'");
				if($result2){

					$id_conversation=mysqli_real_escape_string($connection,$_POST["id_conversazione"]);
					$id_other=mysqli_real_escape_string($connection,$_POST["idOther"]);
					$textForOther=mysqli_real_escape_string($connection,$_POST["textOther"]);
					$textForMe=mysqli_real_escape_string($connection,$_POST["textMy"]);
					$conversation=mysqli_query($connection,"SELECT * FROM Conversation WHERE (user_1='{$id}' AND user_2='{$id_other}') OR (user_1='{$id_other}' AND user_2='{$id}')");
					if($id_conversation!="0" || mysqli_num_rows($conversation)>0 ){

						$conversazione=mysqli_query($connection,"SELECT * FROM Conversation WHERE id_conversation='{$id_conversation}'");
						if($conversazione){
							$insertMessage=mysqli_query($connection,"INSERT INTO Message (id_message,text,id_user,id_conversation,is_image) VALUES (null,'{$textForOther}','{$id}','{$id_conversation}','{$_POST["isImage"]}')");
							if($insertMessage){
								$id_message=mysqli_insert_id($connection);
								$insertMessageMe=mysqli_query($connection,"INSERT INTO MessageForMe (id_message,id_conversation,id_user,text) VALUES ('{$id_message}','{$id_conversation}','{$id}','{$textForMe}')");
								if($insertMessageMe){
									$response["id_message"]=$id_message;
									$ora=mysqli_query($connection,"SELECT * FROM Message WHERE id_message='{$id_message}'");
									$ora=mysqli_fetch_assoc($ora);
									$response["ora"]=$ora["createt_at"];
									$user2=mysqli_query($connection,"SELECT * FROM User WHERE id_user='{$id_other}'");
									if($user2){
										$user2=mysqli_fetch_assoc($user2);
										$keyGcm=mysqli_query($connection,"SELECT * FROM Chiavi where id_key='{$user2['id_key']}'");
										if($keyGcm){
											$keyGcm=mysqli_fetch_assoc($keyGcm);
											$gcm=$keyGcm["gcmRegistration"];
											if($gcm!=null){

												$aes = new AES();
												$aes->setKey("caputotavellamantovani99");
												$aes->setBlockSize(256);
												$aes->setData($gcm);
												$gcm=$aes->decrypt();
												$data=array();
												$data["id_messaggio"]=$id_message;
												$data["id_conversation"]=$id_conversation;
												$data["mittente"]=$user2["name"];
												$data["id_mittente"]=$id;
												//aggiungere url image e publicKey del mittente cosi da creare un nuovo elementp
												//aggiungere anche e mail
												//controllare qusto flag se message oppure image
												if($_POST["isImage"]=="0"){
													$data["flag"]="50";
													$message=mysqli_fetch_assoc(mysqli_query($connection,"SELECT * FROM Message WHERE id_message='{$id_message}'"));
													$data["message"]=$message["text"];

												}else{
														$data["flag"]="100";

														$base=$_POST['textMy'];
														$filename=$_POST['filename'];
														$array=explode(".",$filename);
														if(isset($array[1]))
															$filename=$id_message.".".$array[1];
														else
															$filename=$id_message.".jpg";
														$binary=base64_decode($base);
														header('Content-Type: bitmap; charset=utf-8');
														$file = fopen('imageMessage/'.$filename, 'wb');
														$name="imageMessage/".$filename;
														fwrite($file, $binary);
														fclose($file);
														$data["message"]=$name;
														mysqli_query($connection,"UPDATE Message SET text='{$name}' WHERE id_message='{$id_message}'");
														mysqli_query($connection,"UPDATE MessageForMe SET text='{$name}' WHERE id_message='{$id_message}'");
														$messaggioImage=mysqli_query($connection,"SELECT * FROM Message WHERE id_message='{$id_message}'");
														$messaggioImage=mysqli_fetch_assoc($messaggioImage);
														$response["text"]=$messaggioImage["text"];

												}
												$result2=mysqli_query($connection,"SELECT * FROM Chiavi WHERE id_key='{$result1['id_key']}'");
												$result2=mysqli_fetch_assoc($result2);
												$publicKey=$result2["publicKey"];

												$aes1 = new AES();
												$aes1->setKey("caputotavellamantovani99");
												$aes1->setBlockSize(256);
												$aes1->setData($publicKey);
												$publicKey=$aes1->decrypt();
												$data["publicKey"]=$publicKey;
												$image=mysqli_query($connection,"SELECT * FROM Image WHERE id_user='{$result1['id_user']}'");
												$image=mysqli_fetch_assoc($image);
												$data["urlImage"]=$image["url"];

												$data["ora"]=date('Y-m-d G:i:s');
												$apiKey=GOOGLE_API_KEY;
												$fields=array(
													'to' => $gcm,
													'data' => $data,
												);
												$url='https://gcm-http.googleapis.com/gcm/send';
												$headers = array(
													'Authorization: key=' . $apiKey,
													'Content-Type: application/json'
												);

												$ch = curl_init();

												// Set the url, number of POST vars, POST data
												curl_setopt($ch, CURLOPT_URL, $url);
												curl_setopt($ch, CURLOPT_POST, true);
												curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
												curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

												// Abling SSL Certificate support temporarly
												curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);

												curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));


												// Execute post
												$result = curl_exec($ch);
												if ($result === FALSE) {
													$response['errore'] = TRUE;
													$response['risultato'] = 'Unable to send test push notification';
													echo json_encode($response);
													exit;
												}

												// Close connection
												curl_close($ch);
												$response['errore'] = FALSE;
												$response['risultato'] = 'Test push message sent successfully!';


											}else{
												//devo inserire cmq il messaggio e farlo scaricare all'utente
												//il messaggio è cmq inserito qua nel db
												$response['errore'] = FALSE;
												$response['risultato'] = 'gcmNull';

											}
										}else{
											$response['errore'] = TRUE;
											$response['risultato'] = 'user non esistente gcm';
										}

									}else{
										$response['errore'] = TRUE;
										$response['risultato'] = 'user destinataio non esistente';
									}

									//send message
								}else{
									$response["errore"]=true;
									$response["risultato"]="errore insert messageMe";
								}
							}else{
								$response["errore"]=true;
								$response["risultato"]="errore insert messageOther";
							}
						}else{
							$response["errore"]=true;
							$response["risultato"]="errore select conversation";
						}
					}else{
						//se non esiste allora la devo creare
						$conversazione=mysqli_query($connection,"INSERT INTO Conversation (user_1,user_2) VALUES ('{$id}','{$id_other}')");
						if($conversazione){
							$conversazioneId=mysqli_insert_id($connection);
							$have=mysqli_query($connection,"INSERT INTO Have (id_user,id_conversation) VALUES ('{$id}','{$conversazioneId}')");
							if($have){
								$haveOther=mysqli_query($connection,"INSERT INTO Have (id_user,id_conversation) VALUES ('{$id_other}','{$conversazioneId}')");
								if($haveOther){
									$insertMessage=mysqli_query($connection,"INSERT INTO Message (id_message,text,id_user,id_conversation) VALUES (null,'{$textForOther}','{$id}','{$conversazioneId}')");
									if($insertMessage){
										$id_message=mysqli_insert_id($connection);
										$insertMessageMe=mysqli_query($connection,"INSERT INTO MessageForMe (id_message,id_conversation,id_user,text) VALUES ('{$id_message}','{$conversazioneId}','{$id}','{$textForMe}')");
										if($insertMessageMe){
											$response["errore"]=false;
											$response["id_message"]=$id_message;
											$ora=mysqli_query($connection,"SELECT * FROM Message WHERE id_message='{$id_message}'");
											$ora=mysqli_fetch_assoc($ora);
											$response["ora"]=$ora["createt_at"];
											$response["id_conversation"]=$conversazioneId;
											//send message
											$user2=mysqli_query($connection,"SELECT * FROM User WHERE id_user='{$id_other}'");
											if($user2){
												$user2=mysqli_fetch_assoc($user2);
												$keyGcm=mysqli_query($connection,"SELECT * FROM Chiavi where id_key='{$user2['id_key']}'");
												if($keyGcm){
													$keyGcm=mysqli_fetch_assoc($keyGcm);
													$gcm=$keyGcm["gcmRegistration"];
													if($gcm!=null){

														$aes = new AES();
														$aes->setKey("caputotavellamantovani99");
														$aes->setBlockSize(256);
														$aes->setData($gcm);
														$gcm=$aes->decrypt();
														$data=array();
														$data["id_messaggio"]=$id_message;
														$data["id_conversation"]=$conversazioneId;
														$data["mittente"]=$result1["name"];
														$data["id_mittente"]=$id;
														$data["ora"]=date('Y-m-d G:i:s');
														if($_POST["isImage"]=="0"){
															$data["flag"]="50";
															$message=mysqli_fetch_assoc(mysqli_query($connection,"SELECT * FROM Message WHERE id_message='{$id_message}'"));
															$data["message"]=$message["text"];

															}else{
																$data["flag"]="100";
																$base=$_POST['textMy'];
																$filename=$_POST['filename'];
																$array=explode(".",$filename);
																if(isset($array[1]))
																	$filename=$id_message.".".$array[1];
																else
																	$filename=$id_message.".jpg";$binary=base64_decode($base);
																header('Content-Type: bitmap; charset=utf-8');
																$file = fopen('imageMessage/'.$filename, 'wb');
																$name="imageMessage/".$filename;
																fwrite($file, $binary);
																fclose($file);
																$data["message"]=$name;
																mysqli_query($connection,"UPDATE Message SET text='{$name}' WHERE id_message='{$id_message}'");
																mysqli_query($connection,"UPDATE MessageForMe SET text='{$name}' WHERE id_message='{$id_message}'");
																$messaggioImage=mysqli_query($connection,"SELECT * FROM Message WHERE id_message='{$id_message}'");
																$messaggioImage=mysqli_fetch_assoc($messaggioImage);
																$response["text"]=$messaggioImage["text"];

														}
														$data["email"]=$result1['email'];

														$result2=mysqli_query($connection,"SELECT * FROM Chiavi WHERE id_key='{$result1['id_key']}'");

														$result2=mysqli_fetch_assoc($result2);
														$publicKey=$result2["publicKey"];

														$aes1 = new AES();
														$aes1->setKey("caputotavellamantovani99");
														$aes1->setBlockSize(256);
														$aes1->setData($publicKey);

														$publicKey=$aes1->decrypt();

														$data["publicKey"]=$publicKey;

														$image=mysqli_query($connection,"SELECT * FROM Image WHERE id_user='{$result1['id_user']}'");
														$image=mysqli_fetch_assoc($image);
														$data["urlImage"]=$image["url"];


														$apiKey=GOOGLE_API_KEY;
														$fields=array(
															'to' => $gcm,
															'data' => $data,
														);
														$url='https://gcm-http.googleapis.com/gcm/send';
														$headers = array(
															'Authorization: key=' . $apiKey,
															'Content-Type: application/json'
														);

														$ch = curl_init();

														// Set the url, number of POST vars, POST data
														curl_setopt($ch, CURLOPT_URL, $url);
														curl_setopt($ch, CURLOPT_POST, true);
														curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
														curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

														// Abling SSL Certificate support temporarly
														curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);

														curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));


														// Execute post
														$result = curl_exec($ch);
														if ($result === FALSE) {
															$response['errore'] = TRUE;
															$response['risultato'] = 'Unable to send test push notification';
															echo json_encode($response);
															exit;
														}

														// Close connection
														curl_close($ch);
														$response['errore'] = FALSE;
														$response['risultato'] = 'Test push message sent successfully!';



													}else{
														$response['errore'] = FALSE;
														$response['risultato'] = 'gcmNull';
													}
												}else{
													$response['errore'] = TRUE;
													$response['risultato'] = 'user non esistente gcm';
												}

											}else{
												$response['errore'] = TRUE;
												$response['risultato'] = 'user destinataio non esistente';
											}

										}else{
											$response["errore"]=true;
											$response["risultato"]="errore insert messageMe";

										}
									}else{
										$response["errore"]=true;
										$response["risultato"]="errore mess insert other";
									}
								}else{
									$response["errore"]=true;
									$response["risultato"]="errore have inser other";
								}
							}else{
								$response["errore"]=true;
								$response["risultato"]="errore insert haveMe";
							}
						}else{
							$response["errore"]=true;
							$response["risultato"]="errore insert conversation";
						}

					}

				}else{
					$response["errore"]=true;
					$response["risultato"]="errore utente pwd ";
				}
			}else{
				$response["errore"]=true;
				$response["risultato"]="errore utente nonEsistente ";
			}
		}else{
			$response["errore"]=true;
			$response["risultato"]="errore connessione ";
		}
		echo json_encode($response);

	}

?>
