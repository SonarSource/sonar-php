<?php

// Stream socket

// Recent SSL Protocol Versions

$ctx = stream_context_create([
    'ssl' => [
        'crypto_method' => STREAM_CRYPTO_METHOD_SSLv3_CLIENT
    ],
]);

$ctx = stream_context_create([
    'ssl' => [
        'crypto_method' => STREAM_CRYPTO_METHOD_SSLv3_SERVER
    ],
]);

stream_socket_enable_crypto($ctx, true, STREAM_CRYPTO_METHOD_TLSv1_2_SERVER);

// Unknown SSL Protocol Used

stream_context_create();

stream_context_create(getConfig());

$ctx = stream_context_create([
    'ssl' => getSSLConfig()
]);

$ctx = stream_context_create([
    'ssl' => [
        'crypto_method' => getSSLProtocol()
    ],
]);

stream_socket_enable_crypto($ctx, true, getSSLProtocol());

stream_socket_enable_crypto($ctx, true);

stream_context_create([
        'http'=>[
            'method'=>"GET"
        ]
]);

// Older SSL Protocol Versions

$ctx = stream_context_create([
    'ssl' => [
        'crypto_method' => STREAM_CRYPTO_METHOD_TLSv1_0_CLIENT, // Noncompliant{{Change this code to use a stronger protocol.}}
//                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    ],
]);

$ctx = stream_context_create(array(
    'ssl' => [
        'crypto_method' => STREAM_CRYPTO_METHOD_TLSv1_0_CLIENT, // Noncompliant
//                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    ],
));

$ctx = stream_context_create(array(
    'ssl' => array(
        'crypto_method' => STREAM_CRYPTO_METHOD_TLSv1_0_CLIENT, // Noncompliant
//                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    )
));

stream_socket_enable_crypto($$ctx, true, STREAM_CRYPTO_METHOD_SSLv23_CLIENT); // Noncompliant

// Using variable for configuration

$cryptoMethod = STREAM_CRYPTO_METHOD_ANY_CLIENT; // Noncompliant

$ctx = stream_context_create([
    'ssl' => [
        'crypto_method' => $cryptoMethod
    ],
]);

$ctxConfig = ['ssl' => [
'crypto_method' => STREAM_CRYPTO_METHOD_ANY_CLIENT // Noncompliant
]];

$ctx = Stream_Context_Create($ctxConfig);

$sslConfig = [
'crypto_method' => STREAM_CRYPTO_METHOD_ANY_CLIENT // Noncompliant
];

$ctx = stream_context_create([
    'ssl' => $sslConfig,
]);

$maybeSSLConfig = [
'crypto_method' => STREAM_CRYPTO_METHOD_ANY_CLIENT
];

if (condition()) {
    $maybeSSLConfig = [
        'crypto_method' => STREAM_CRYPTO_METHOD_SSLv3_CLIENT
    ];
}

$ctx = stream_context_create([
    'ssl' => $maybeSSLConfig,
]);

$unused = STREAM_CRYPTO_METHOD_ANY_CLIENT;

$sslv23 = STREAM_CRYPTO_METHOD_SSLv23_CLIENT; // Noncompliant

stream_socket_enable_crypto($$ctx, true, $sslv23);

// Using Bitwise OR

$ctx = stream_context_create([
    'ssl' => [
        'crypto_method' => STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT | // Noncompliant
                            STREAM_CRYPTO_METHOD_TLSv1_1_SERVER, // Noncompliant
    ],
]);

$ctx = stream_context_create([
    'ssl' => [
        'crypto_method' => STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT | // Noncompliant
                            STREAM_CRYPTO_METHOD_TLSv1_1_SERVER | // Noncompliant
                            STREAM_CRYPTO_METHOD_TLSv1_2_SERVER
    ],
]);

$ctx = stream_context_create([
    'ssl' => [
        'crypto_method' => getOthers() |
                           STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT // Noncompliant
    ],
]);
$ctx = stream_context_create(
  params:['ssl' => ['crypto_method' => STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT]],
  options:['ssl' => ['crypto_method' => STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT]] // Noncompliant
);

stream_socket_enable_crypto($fp, true, STREAM_CRYPTO_METHOD_TLSv1_2_SERVER | STREAM_CRYPTO_METHOD_TLSv1_1_SERVER); // Noncompliant
stream_socket_enable_crypto($fp, crypto_type:STREAM_CRYPTO_METHOD_TLSv1_1_SERVER, enable:true); // Noncompliant
stream_socket_enable_crypto($fp, crypto_type:STREAM_CRYPTO_METHOD_TLSv1_2_SERVER, enable:true);

function arrayAssignment()
{
  $options = [];
  $options['ssl']['crypto_method'] = \STREAM_CRYPTO_METHOD_TLS_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT; // Noncompliant
  $options['ssl']['crypto_method'] ??= \STREAM_CRYPTO_METHOD_TLS_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT; // Compliant -- the assignment is conditional
  stream_context_create($options);
}
function arrayAssignment2()
{
  $options = [];
  $options['ssl'] = [
    'crypto_method' => \STREAM_CRYPTO_METHOD_TLS_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT // Noncompliant
  ];
  $options['ssl']['crypto_method'] ??= \STREAM_CRYPTO_METHOD_TLS_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT; // Compliant -- the assignment is conditional
  stream_context_create($options);
}
function arrayAssignment3()
{
  $options = [];
  $options['ssl']['crypto_method'] = \STREAM_CRYPTO_METHOD_TLS_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT; // Compliant -- reassigned later
  $options['ssl'] = [
    'crypto_method' => \STREAM_CRYPTO_METHOD_TLS_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT // Noncompliant
  ];
  $options['ssl']['crypto_method'] ??= \STREAM_CRYPTO_METHOD_TLS_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT | \STREAM_CRYPTO_METHOD_TLSv1_1_CLIENT; // Compliant -- the assignment is conditional
  stream_context_create($options);
}

// Curl

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://www.sonarsource.com/');
curl_setopt($ch, CURLOPT_SSLVERSION, CURL_SSLVERSION_TLSv1_2);

curl_setopt($ch, CURLOPT_SSLVERSION);

$tlsv1_2 = CURL_SSLVERSION_TLSv1_2;
curl_setopt($ch, CURLOPT_SSLVERSION, $tlsv1_2);

$unused_sslv2 = CURL_SSLVERSION_SSLv2;

curl_setopt($ch, CURLOPT_SSLVERSION, CURL_SSLVERSION_TLSv1); // Noncompliant
curl_setopt($ch, CURLOPT_SSLVERSION, CURL_SSLVERSION_SSLv2); // Noncompliant
curl_setopt($ch, CURLOPT_SSLVERSION, CURL_SSLVERSION_SSLv3); // Noncompliant
curl_setopt($ch, CURLOPT_SSLVERSION, CURL_SSLVERSION_TLSv1_0); // Noncompliant
curl_setopt($ch, CURLOPT_SSLVERSION, CURL_SSLVERSION_TLSv1_1); // Noncompliant

$sslv3 = CURL_SSLVERSION_SSLv3; // Noncompliant

curl_setopt($ch, CURLOPT_SSLVERSION, $sslv3);

curl_setopt($ch, CURLOPT_OTHER_KEY, CURL_SSLVERSION_SSLv3);
curl_setopt($ch, foo(), CURL_SSLVERSION_SSLv3);

curl_setopt($ch, value:CURL_SSLVERSION_TLSv1_2, option:CURLOPT_SSLVERSION);
curl_setopt($ch, value:CURL_SSLVERSION_TLSv1_1, option:CURLOPT_SSLVERSION); // Noncompliant
curl_setopt($ch, value:CURL_SSLVERSION_TLSv1_1);
curl_setopt($ch, option:CURLOPT_SSLVERSION);
