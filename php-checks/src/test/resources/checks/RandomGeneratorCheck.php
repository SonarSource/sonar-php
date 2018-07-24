<?php

$random = rand(); // Noncompliant {{Use a cryptographically strong random number generator instead.}}
//        ^^^^^^
$random = rand(1, 2); // Noncompliant

$random2 = mt_rand(0, 99); // Noncompliant
//         ^^^^^^^^^^^^^^
$random2 = mt_rand(); // Noncompliant

$random = random_int(0, 100);
$random = random_bytes(128);
$random = openssl_random_pseudo_bytes(96);
