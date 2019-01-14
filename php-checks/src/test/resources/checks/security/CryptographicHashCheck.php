<?php

hash('md5', $data, false); // Noncompliant

$ctx = hash_init('sha384'); // Noncompliant
//     ^^^^^^^^^^^^^^^^^^^
$ctx = hash_init('sha384', HASH_HMAC); // Compliant
$ctx = hash_init('sha384', HASH_HMAC, 'secret'); // Compliant

$hashed_password = crypt('mypassword'); // Noncompliant

$hashed_password = password_hash("rasmuslerdorf", PASSWORD_BCRYPT); // Noncompliant

$hash = hash_pbkdf2("sha256", $password, $salt, $iterations, 20); // Noncompliant

$generated_key = openssl_pbkdf2($password, $salt, $keyLength, $iterations, 'sha256'); // Noncompliant

$hash = md5($str); // Noncompliant

$hash = sha1($str); // Noncompliant

// out of scope
$hash = hash_file('md5', 'example.txt');
$hash = md5_file($file);
$hash = sha1_file($file);
$hash = hash_hmac_file('md5', 'example.txt', 'secret');
$hash = hash_hmac('ripemd160', 'The quick brown fox jumped over the lazy dog.', 'secret');

// coverage
$value = 0;
$ctx = hash_init('sha384', $value); // Noncompliant
$ctx = HASH_INIT('sha384', CONSTANT_VALUE); // Noncompliant
