<?php

$ciphertext = mcrypt_encrypt(MCRYPT_DES, $key, $plaintext, $mode);  // Noncompliant {{Use the recommended AES (Advanced Encryption Standard) instead.}}
//                           ^^^^^^^^^^

$ciphertext = mcrypt_encrypt(MCRYPT_DES_COMPAT, $key, $plaintext, $mode); // Noncompliant

$cipher = MCRYPT_TRIPLEDES; // Noncompliant
$ciphertext = mcrypt_encrypt($cipher, $key, $plaintext, $mode);

$ciphertext = mcrypt_encrypt(MCRYPT_3DES, $key, $plaintext, $mode); // Noncompliant


$plaintext = "message to be encrypted";
$ivlen = openssl_cipher_iv_length($cipher = "des-ede3-cbc");  // Noncompliant
//                                          ^^^^^^^^^^^^^^
$iv = openssl_random_pseudo_bytes($ivlen);
$ciphertext_raw = openssl_encrypt($plaintext, $cipher, $key, $options = OPENSSL_RAW_DATA, $iv);
$unrelatedStuff = "This literal contains des-ede3 but doesn't raise the issue"; // OK
