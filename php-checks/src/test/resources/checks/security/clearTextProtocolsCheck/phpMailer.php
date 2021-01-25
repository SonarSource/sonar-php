<?php

use PHPMailer\PHPMailer\PHPMailer;

$mail1 = new PHPMailer(true);
$mail1->Host = 'test.com';
$mail1->SMTPSecure = ''; // Noncompliant

$mail2 = new PHPMailer(true); // Noncompliant
$mail2->Host = 'test.com';

$mail3 = new PHPMailer(true);
$mail3->Host = 'ssl://test.com'; // Compliant

$mail4 = new PHPMailer(true);
$mail4->Host = 'tls://test.com'; // Compliant

$mail5 = new PHPMailer(true);
$mail5->Host = 'tls://test.com'; // Compliant
$mail5->SMTPSecure = '';

$mail6 = new PHPMailer(true);
$mail6->Host = 'test.com';
$mail6->SMTPSecure = 'tls'; // Compliant

$mail7 = new PHPMailer(true);
$mail7->Host = 'test.com';
$mail7->SMTPSecure = 'ssl'; // Compliant

$mail8 = new PHPMailer(true);
$mail8->Host = '127.0.0.1'; // Compliant
$mail8->SMTPSecure = '';

$mail9 = new PHPMailer(); // Compliant
$mail9->Host = $host;

$mail10 = new PHPMailer();
$mail10->$field = "foo"; // Compliant - dynamic field access
$mail10->noRelevant = "ssl";
$mail10->SMTPSecure;
$something = $mail10->SMTPSecure;

$mail11 = new WrongNamespace\PHPMailer(); // Compliant
$mail11->SMTPSecure = "";
$mail12 = new $foo(); // Compliant
$mail13 = new $foo; // Compliant
$mail14 = new PHPMailer; // FN - non call construction is not handled
$unknown->SMTPSecure = ''; // Compliant
