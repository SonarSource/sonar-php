<?php

$verify_host_on = '2';
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, $verify_host_on);
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, '2');
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, 2);
other_func($curl, CURLOPT_SSL_VERIFYHOST, 0);

$verify_peer_on = TRUE;
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, $verify_peer_on);
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, true);
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, '1');
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, 1);
other_func($curl, CURLOPT_SSL_VERIFYPEER, 0);
curl_setopt($curl, CURLOPT_OTHER_KEY, 0);

$verify_host_off = FALSE;
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, $verify_host_off); // Noncompliant {{Activate SSL/TLS certificates chain of trust verification.}}
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, TRUE); // Noncompliant
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, '0'); // Noncompliant
curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, 0); // Noncompliant

$verify_peer_off = '0';
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, FALSE); // Noncompliant
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, $verify_peer_off); // Noncompliant
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, 0); // Noncompliant