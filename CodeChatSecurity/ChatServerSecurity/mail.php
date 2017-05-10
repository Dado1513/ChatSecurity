<?php

require "phpmailer/class.phpmailer.php";
//PHPMailer Object
$mail = new PHPMailer;

//From email address and name
$mail->FromName = "ChatSecurity";

//To address and name
$mail->addAddress("rcaputo99@libero.it", "Davide");

//Address to which recipient will reply
//$mail->addReplyTo("reply@yourdomain.com", "Reply");
/*
//CC and BCC
$mail->addCC("cc@example.com");
$mail->addBCC("bcc@example.com");
*/
//Send HTML or Plain Text email
$mail->isHTML(true);

$mail->Subject = "Subject Text";
$mail->Body = "<i>Mail body in HTML</i>";
$mail->AltBody = "This is the plain text version of the email content";

if(!$mail->send()) 
{
    echo "Mailer Error: " . $mail->ErrorInfo;
} 
else 
{
    echo "Message has been sent successfully";
}
?>