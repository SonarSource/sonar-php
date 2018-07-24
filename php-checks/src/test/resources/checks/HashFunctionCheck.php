<?php
$email = $_GET['email'];
$name = $_GET['name'];
$password = $_GET['password'];

$hash = hash_pbkdf2('sha256', $password, $email, 100000); // Noncompliant {{Use an unpredictable salt value.}}

$hash = hash_pbkdf2('sha256', $password, $_POST['user'], 100000); // Noncompliant

$hash = hash_pbkdf2('sha256', $password, '', 100000); // Noncompliant

$hash = hash_pbkdf2('sha256', $password, 'D8VxSmTZt2E2YV454mkqAY5e', 100000); // Noncompliant

$salt = 'salt';
$hash = hash_pbkdf2('sha256', $password, $salt, 100000); // Noncompliant

$salt2 = openssl_random_pseudo_bytes(16);
$hash = hash_pbkdf2("sha256", $password, $salt2, $iterations, 20);

$salt3 = $arr['email'];
$hash = hash_pbkdf2("sha256", $password, $salt3, $iterations, 20);

$salt4 = $salt;
$hash = hash_pbkdf2("sha256", $password, $salt4, $iterations, 20); // Noncompliant

$salt5 = 'salt';
$salt5 = 'salt';
$hash = hash_pbkdf2("sha256", $password, $salt5, $iterations, 20); // FN

