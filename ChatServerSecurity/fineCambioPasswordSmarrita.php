<?php
	include('include/config.php');
	$username = $_POST['username'];
	
	$connection = mysqli_connect(DB_HOST, DB_USERNAME, DB_PASSWORD,DB_NAME);
	$q = mysqli_query($connection, "SELECT * FROM User WHERE name='{$username}'") or die("query sbagliata");
	$q = mysqli_fetch_assoc($q);
	$idKey = $q['id_key'];
	$password = md5($_POST['first']);
	mysqli_query($connection, "UPDATE Chiavi SET password='{$password}' WHERE id_key={$idKey}") or die("non funziona la query di update");
	
?>
