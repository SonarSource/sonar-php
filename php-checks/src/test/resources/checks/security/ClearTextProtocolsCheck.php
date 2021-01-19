<?php

$url = "http://"; // Noncompliant {{Using http protocol is insecure. Use https instead}}
$url = "http://exemple.com"; // Noncompliant
$url = "http://0001::1"; // Noncompliant
$url = "http://dead:beef::1"; // Noncompliant
$url = "http://::dead:beef:1"; // Noncompliant
$url = "http://192.168.0.1"; // Noncompliant
$url = "http://10.1.1.123"; // Noncompliant
$url = "http://subdomain.exemple.com"; // Noncompliant

$url = "ftp://"; // Noncompliant {{Using ftp protocol is insecure. Use sftp, scp or ftps instead}}
$url = "ftp://anonymous@exemple.com"; // Noncompliant
$url = "telnet://"; // Noncompliant {{Using telnet protocol is insecure. Use ssh instead}}
$url = "telnet://anonymous@exemple.com"; // Noncompliant

function test(string $xxx = 'http://test.com') {} // Noncompliant

// Non sensitive url scheme
$url = "https://"; // Compliant
$url = "sftp://"; // Compliant
$url = "ftps://"; // Compliant
$url = "scp://"; // Compliant
$url = "ssh://"; // Compliant

// Only report string staring with the sensitive url scheme
$doc = "See http://exemple.com"; // Compliant
$doc = "See ftp://exemple.com"; // Compliant
$doc = "See telnet://exemple.com"; // Compliant

// The url domain component is a loopback address
$url = "http://localhost"; // Compliant
$url = "http://127.0.0.1"; // Compliant
$url = "http://::1"; // Compliant
$url = "ftp://user@localhost"; // Compliant
$url = 123; // Compliant
