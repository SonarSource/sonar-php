<?php

$verify_host_on = '2';
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, $verify_host_on);
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, '2');
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, 2);
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, TRUE);
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, 1);
other_func($curl, CURLOPT_SSL_VERIFYHOST, 0);
curl_setopt($curl, CURLOPT_OTHER_KEY, 0);

$verify_host_off = FALSE;
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, $verify_host_off); // Noncompliant {{Enable server hostname verification on this SSL/TLS connection.}}
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, '0'); // Noncompliant
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, 0); // Noncompliant
curl_setopt($curl, value:0, option:CURLOPT_SSL_VERIFYHOST); // Noncompliant
curl_setopt($curl, value:1, option:CURLOPT_SSL_VERIFYHOST);


curl_setopt($curl, CURLOPT_OTHER_KEY);
curl_setopt($curl, getCurlOptionKey('host'), getCurlOptionValue('host'));
