<?php

  openssl_public_encrypt($data, $crypted, $key, OPENSSL_NO_PADDING);    // Noncompliant {{Use secure mode and padding scheme.}}
                                              //^^^^^^^^^^^^^^^^^^
  openssl_public_encrypt($data, $crypted, $key, OPENSSL_PKCS1_PADDING); // Noncompliant
  openssl_public_encrypt($data, $crypted, $key, OPENSSL_SSLV23_PADDING); // Noncompliant
  openssl_public_encrypt($data, $crypted, $key, OPENSSL_PKCS1_OAEP_PADDING);
// by default OPENSSL_PKCS1_PADDING is used
  openssl_public_encrypt($data, $crypted, $key); // Noncompliant
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  openssl_public_encrypt($data, $crypted, padding:$key, key: OPENSSL_SSLV23_PADDING);
  openssl_public_encrypt($data, $crypted, padding:OPENSSL_SSLV23_PADDING, key:$key); // Noncompliant

  openssl_encrypt($plaintext, "BF-ECB", $key, OPENSSL_RAW_DATA, $iv); // Noncompliant {{Use secure mode and padding scheme.}}
  openssl_encrypt($plaintext, "RC2-ECB", $key, OPENSSL_RAW_DATA, $iv); // Noncompliant
  openssl_encrypt($plaintext, "bf-ecb", $key, OPENSSL_RAW_DATA, $iv); // Noncompliant
  openssl_encrypt($plaintext, "des-ecb", $key, OPENSSL_RAW_DATA, $iv); // Noncompliant
  openssl_encrypt($plaintext, "rc2-ecb", $key, OPENSSL_RAW_DATA, $iv); // Noncompliant
  openssl_encrypt($plaintext, "aes-256-gcm", $key, OPENSSL_RAW_DATA, $iv); // Compliant
  openssl_encrypt($plaintext, key:"rc2-ecb", method:"aes-256-gcm"); // Compliant
  openssl_encrypt($plaintext, key:"rc2-ecb", method:"rc2-ecb"); // Noncompliant

  $mode = "ecb";
  mcrypt_encrypt(MCRYPT_DES, $key, $plaintext, "ecb"); // Noncompliant {{Use secure mode and padding scheme.}}
  mcrypt_encrypt(MCRYPT_DES_COMPAT, $key, $plaintext, $mode); // Noncompliant
  mcrypt_encrypt(MCRYPT_RC4, $key, $plaintext, "ecb"); // Noncompliant
  mcrypt_encrypt(MCRYPT_RC4, $key, $plaintext, "cbc"); // Compliant
  // mcrypt_encrypt was removed in PHP 7.2, so there's no need to test PHP named arguments on it

  foo($data, $crypted, $key, OPENSSL_NO_PADDING);
  openssl_public_encrypt($data, $crypted, $key, getPadding());
  mcrypt_encrypt(MCRYPT_RC4, $key, $plaintext);
