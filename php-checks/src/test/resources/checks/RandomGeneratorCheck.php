<?php

$random = rand(); // Noncompliant {{Use a cryptographically strong random number generator instead.}}
//        ^^^^^^

$random2 = mt_rand(0, 99); // Noncompliant
//         ^^^^^^^^^^^^^^

$random = random_int();
$random = random_bytes(128);
$random = openssl_random_pseudo_bytes(96);
