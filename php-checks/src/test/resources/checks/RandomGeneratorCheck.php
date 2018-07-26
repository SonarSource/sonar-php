<?php

$random = rand(); // Noncompliant {{Make sure that using this pseudorandom number generator is safe here.}}
//        ^^^^^^
$random = rand(1, 2); // Noncompliant

$random2 = mt_rand(0, 99); // Noncompliant
//         ^^^^^^^^^^^^^^
$random2 = mt_rand(); // Noncompliant

$random = random_int(0, 100);
$random = random_bytes(128);
$random = openssl_random_pseudo_bytes(96);
