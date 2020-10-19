<?php
// mcrypt_encrypt is deprecated since PHP 7.1
// mcrypt_encrypt was removed in PHP 7.2, so there's no need to test PHP named arguments on it
$c1 = mcrypt_encrypt(MCRYPT_DES, $key, $plaintext, $mode);  // Noncompliant
$c2 = mcrypt_encrypt(MCRYPT_DES_COMPAT, $key, $plaintext, $mode); // Noncompliant
$c3 = mcrypt_encrypt(MCRYPT_TRIPLEDES, $key, $plaintext, $mode); // Noncompliant

$c4 = mcrypt_encrypt(MCRYPT_3DES, $key, $plaintext, $mode);  // Noncompliant
$c5 = mcrypt_encrypt(MCRYPT_BLOWFISH, $key, $plaintext, $mode);  // Noncompliant
$c6 = mcrypt_encrypt(MCRYPT_RC2, $key, $plaintext, $mode);  // Noncompliant
$mcrypt_rc4 = MCRYPT_RC4;
$c7 = mcrypt_encrypt($mcrypt_rc4, $key, $plaintext, $mode);  // Noncompliant


$c8 = openssl_encrypt($plaintext, "bf-ecb", $key, OPENSSL_RAW_DATA, $iv); // Noncompliant
$c9 = openssl_encrypt($plaintext, "des-ede3", $key, OPENSSL_RAW_DATA, $iv);  // Noncompliant
$c10 = openssl_encrypt($plaintext, "des-ofb", $key, OPENSSL_RAW_DATA, $iv);  // Noncompliant
$c11 = openssl_encrypt($plaintext, "rc2-cbc", $key, OPENSSL_RAW_DATA, $iv); // Noncompliant
$c12 = openssl_encrypt($plaintext, "rc4", $key, OPENSSL_RAW_DATA, $iv);  // Noncompliant

$c13 = openssl_encrypt($plaintext, "aes-256-gcm", $key, OPENSSL_RAW_DATA, $iv); // Compliant

$c13 = openssl_encrypt($plaintext, key:$key, method:"aes-256-gcm"); // Compliant
$c13 = openssl_encrypt($plaintext, key:$key, method:"rc4"); // Noncompliant

function getAlgorithm() {
  return "rc4";
}
$c13 = openssl_encrypt($plaintext, getAlgorithm(), $key, $options=OPENSSL_RAW_DATA, $iv); // Compliant - FN
