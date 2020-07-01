<?php

  openssl_public_encrypt($data, $crypted, $key, OPENSSL_NO_PADDING);    // Noncompliant {{Use secure mode and padding scheme.}}
                                              //^^^^^^^^^^^^^^^^^^
  openssl_public_encrypt($data, $crypted, $key, OPENSSL_PKCS1_PADDING); // Noncompliant
  openssl_public_encrypt($data, $crypted, $key, OPENSSL_SSLV23_PADDING); // Noncompliant
  openssl_public_encrypt($data, $crypted, $key, OPENSSL_PKCS1_OAEP_PADDING);
// by default OPENSSL_PKCS1_PADDING is used
  openssl_public_encrypt($data, $crypted, $key); // Noncompliant
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  foo($data, $crypted, $key, OPENSSL_NO_PADDING);
  openssl_public_encrypt($data, $crypted, $key, $padding);

  openssl_encrypt($plaintext, "BF-ECB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant {{Use secure mode and padding scheme.}}
  openssl_encrypt($plaintext, "RC2-ECB", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
  openssl_encrypt($plaintext, "bf-ecb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
  openssl_encrypt($plaintext, "des-ecb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
  openssl_encrypt($plaintext, "rc2-ecb", $key, $options=OPENSSL_RAW_DATA, $iv); // Noncompliant
  openssl_encrypt($plaintext, "aes-256-gcm", $key, $options=OPENSSL_RAW_DATA, $iv); // Compliant

  $mode = "ecb";
  mcrypt_encrypt(MCRYPT_DES, $key, $plaintext, "ecb"); // Noncompliant {{Use secure mode and padding scheme.}}
  mcrypt_encrypt(MCRYPT_DES_COMPAT, $key, $plaintext, $mode); // Noncompliant
  mcrypt_encrypt(MCRYPT_RC4, $key, $plaintext, "ecb"); // Noncompliant
  mcrypt_encrypt(MCRYPT_RC4, $key, $plaintext, "cbc"); // Compliant

