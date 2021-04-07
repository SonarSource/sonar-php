<?php
$ciphertext = mcrypt_encrypt(MCRYPT_DES, $key, $plaintext, $mode); // Noncompliant
$ciphertext = mcrypt_encrypt(MCRYPT_DES_COMPAT, $key, $plaintext, $mode); // Noncompliant
$ciphertext = mcrypt_encrypt(MCRYPT_TRIPLEDES, $key, $plaintext, $mode); // Noncompliant
$ciphertext = mcrypt_encrypt(MCRYPT_3DES, $key, $plaintext, $mode); // Noncompliant
$ciphertext = mcrypt_encrypt(MCRYPT_BLOWFISH, $key, $plaintext, $mode); // Noncompliant
$ciphertext = mcrypt_encrypt(MCRYPT_RC2, $key, $plaintext, $mode); // Noncompliant
$mcrypt_rc4 = MCRYPT_RC4;
$c7 = mcrypt_encrypt($mcrypt_rc4, $key, $plaintext, $mode);  // Noncompliant

$ciphertext_raw = openssl_encrypt($plaintext, "BF-CBC", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "BF-ECB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "BF-CFB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "BF-OFB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-CBC", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-CFB1", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-CFB8", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-EDE", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-EDE-CBC", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-EDE-CFB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-EDE-OFB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-EDE3", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-EDE3-CBC", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-EDE3-CFB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-EDE3-CFB1", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-EDE3-CFB8", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-EDE3-OFB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DES-OFB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "DESX-CBC", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "RC2-40-CBC", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "RC2-64-CBC", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "RC2-CBC", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "RC2-CFB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "RC2-ECB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "RC2-OFB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "RC4", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "RC4-40", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "RC4-HMAC-MD5", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "bf-cbc", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "bf-cfb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "bf-ecb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "bf-ofb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-cbc", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-cfb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-cfb1", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-cfb8", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-ecb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-ede", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-ede-cbc", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-ede-cfb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-ede-ofb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-ede3", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-ede3-cbc", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-ede3-cfb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-ede3-cfb1", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-ede3-cfb8", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-ede3-ofb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "des-ofb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "desx-cbc", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "rc2-40-cbc", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "rc2-64-cbc", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "rc2-cbc", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "rc2-cfb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "rc2-ecb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "rc2-ofb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "rc4", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "rc4-40", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
$ciphertext_raw = openssl_encrypt($plaintext, "rc4-hmac-md5", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant

// mcrypt_encrypt is deprecated since PHP 7.1
// mcrypt_encrypt was removed in PHP 7.2, so there's no need to test PHP named arguments on it

$c13 = openssl_encrypt($plaintext, key:$key, method:"aes-256-gcm"); // Compliant
$c13 = openssl_encrypt($plaintext, key:$key, method:"rc4"); // Noncompliant

function getAlgorithm() {
  return "rc4";
}
$c13 = openssl_encrypt($plaintext, getAlgorithm(), $key, $options=OPENSSL_RAW_DATA, $iv); // Compliant - FN

$ciphertext_raw = openssl_encrypt($plaintext, "aes-256-gcm", $key, $options=OPENSSL_RAW_DATA, $iv); // Compliant
