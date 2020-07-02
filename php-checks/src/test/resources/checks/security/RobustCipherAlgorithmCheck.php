<?php
// mcrypt_encrypt is deprecated since PHP 7.1
$c1 = mcrypt_encrypt(MCRYPT_DES, $key, $plaintext, $mode);  // Noncompliant
$c2 = mcrypt_encrypt(MCRYPT_DES_COMPAT, $key, $plaintext, $mode); // Noncompliant
$c3 = mcrypt_encrypt(MCRYPT_TRIPLEDES, $key, $plaintext, $mode); // Noncompliant

$c4 = mcrypt_encrypt(MCRYPT_3DES, $key, $plaintext, $mode);  // Noncompliant
$c5 = mcrypt_encrypt(MCRYPT_BLOWFISH, $key, $plaintext, $mode);  // Noncompliant
$c6 = mcrypt_encrypt(MCRYPT_RC2, $key, $plaintext, $mode);  // Noncompliant
$mcrypt_rc4 = MCRYPT_RC4;
$c7 = mcrypt_encrypt($mcrypt_rc4, $key, $plaintext, $mode);  // Noncompliant


$c8 = openssl_encrypt($plaintext, "bf-ecb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$c9 = openssl_encrypt($plaintext, "des-ede3", $key, $options=OPENSSL_RAW_DATA, $iv);  // Noncompliant
$c10 = openssl_encrypt($plaintext, "des-ofb", $key, $options=OPENSSL_RAW_DATA, $iv);  // Noncompliant
$c11 = openssl_encrypt($plaintext, "rc2-cbc", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$c12 = openssl_encrypt($plaintext, "rc4", $key, $options=OPENSSL_RAW_DATA, $iv);  // Noncompliant

$c13 = openssl_encrypt($plaintext, "aes-256-gcm", $key, $options=OPENSSL_RAW_DATA, $iv); // Compliant


function getAlgorithm() {
  return "rc4";
}
$c13 = openssl_encrypt($plaintext, getAlgorithm(), $key, $options=OPENSSL_RAW_DATA, $iv); // Compliant - FN
