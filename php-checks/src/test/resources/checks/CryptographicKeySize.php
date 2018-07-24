<?php
function nok() {
    $config = array(
            "private_key_bits" => 1024, // Noncompliant {{Use a key length of at least 2048 bits}}
//                                ^^^^
            "private_key_type" => OPENSSL_KEYTYPE_RSA,
        );
    $res = openssl_pkey_new($config);
}

function nok_var() {
    $key_length = 1024;
    $config = array(
            "private_key_bits" => $key_length, // Noncompliant {{Use a key length of at least 2048 bits}}
//                                ^^^^^^^^^^^
            "private_key_type" => OPENSSL_KEYTYPE_RSA,
        );
    $res = openssl_pkey_new($config);
}


function ok() {
    $config = array(
            "private_key_bits" => 2048,
            "private_key_type" => OPENSSL_KEYTYPE_RSA,
        );
    $res = openssl_pkey_new($config);
}

function multipleValues() {
    $maybeConfig = array(
            "private_key_bits" => 1024,
            "private_key_type" => OPENSSL_KEYTYPE_RSA,
        );
    if (cond()) {
      $maybeConfig =  array(
            "private_key_bits" => 2048,
            "private_key_type" => OPENSSL_KEYTYPE_RSA,
      );
    }
    $res = openssl_pkey_new($config);
}

function noRsa() {
  $config = array(
          "private_key_bits" => 1024,
          "private_key_type" => OPENSSL_KEYTYPE_EC,
      );
  $res = openssl_pkey_new($config);
}

function noNumericLiteralOrVariable() {
    $config = array(
            "private_key_bits" => "1024",
            "private_key_type" => OPENSSL_KEYTYPE_RSA,
        );
    $res = openssl_pkey_new($config);

    $configFromFunctionCall = array(
                "private_key_bits" => getKeyLength(),
                "private_key_type" => OPENSSL_KEYTYPE_RSA,
            );
    $res = openssl_pkey_new($configFromFunctionCall);
}

function unknownKeyType() {
    $config = array(
            "private_key_bits" => 2048,
            "private_key_type" => getKeyType(),
        );
    $res = openssl_pkey_new($config);
}
