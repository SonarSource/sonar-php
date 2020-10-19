<?php

$verify_peer_on = TRUE;
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, $verify_peer_on);
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, true);
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, '1');
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, 1);
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, "yes");
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, "sure!");
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER); // OK - is enabled by default

other_func($curl, CURLOPT_SSL_VERIFYPEER, 0);
curl_setopt($curl, CURLOPT_OTHER_KEY, 0);

$verify_peer_off = '0';
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, FALSE); // Noncompliant {{Enable server certificate validation on this SSL/TLS connection.}}
                                         //^^^^^
CURL_setopt($curl, CURLOPT_SSL_VERIFYPEER, $verify_peer_off); // Noncompliant
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, '0'); // Noncompliant
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, 0); // Noncompliant
curl_setopt($curl, value:0, option:CURLOPT_SSL_VERIFYPEER); // Noncompliant
curl_setopt($curl, value:1, option:CURLOPT_SSL_VERIFYPEER);


function isVerifyPeer() { return false; }
curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, isVerifyPeer()); // FN - Simulation necessary
curl_setopt();
