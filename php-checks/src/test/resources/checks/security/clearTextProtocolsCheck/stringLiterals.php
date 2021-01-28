<?php
/** String literals **/
$url = "http://exemple.com"; // Noncompliant {{Using http protocol is insecure. Use https instead}}
$url = "http://0001::1"; // Noncompliant
$url = "http://dead:beef::1"; // Noncompliant
$url = "http://::dead:beef:1"; // Noncompliant
$url = "http://192.168.0.1"; // Noncompliant
$url = "http://10.1.1.123"; // Noncompliant
$url = "http://subdomain.exemple.com"; // Noncompliant

$url = "ftp://anonymous@exemple.com"; // Noncompliant {{Using ftp protocol is insecure. Use sftp, scp or ftps instead}}
$url = "telnet://anonymous@exemple.com"; // Noncompliant {{Using telnet protocol is insecure. Use ssh instead}}

function test(string $xxx = 'http://someUrl.com') {} // Noncompliant

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

$url = "http://"; // Compliant
$url = "ftp://"; // Compliant
$url = "telnet://"; // Compliant

$url = "http://test.com"; // Compliant
$url = "http://someSubdomain.test.com"; // Compliant
$url = "http://someUrl.com?url=test.com"; // Noncompliant
$url = "http://shortest.com"; // Noncompliant

$url = "http://xmlns.com"; // Compliant
$url = "http://someSubdomain.xmlns.com"; // Noncompliant
$url = "http://someUrl.com?url=xmlns.com"; // Noncompliant

$url = str_replace('http://', '', $foo);  // Compliant
$url = str_replace('http://', '', $foo) . $bar;  // Compliant
$url = 'http://' . $bar; // Noncompliant
$url = 'http://something'; // Noncompliant

$url = "http:///www.php-fig.org/"; // Compliant - Malformed URL (no host part)

return;
