<?php
/** String literals **/
$url = "http://exemple.com"; // Noncompliant {{Using HTTP protocol is insecure. Use HTTPS instead.}}
$url = "http://0001::1"; // Noncompliant
$url = "http://dead:beef::1"; // Noncompliant
$url = "http://::dead:beef:1"; // Noncompliant
$url = "http://192.168.0.1"; // Noncompliant
$url = "http://10.1.1.123"; // Noncompliant
$url = "http://subdomain.exemple.com"; // Noncompliant

$url = "ftp://anonymous@exemple.com"; // Noncompliant {{Using FTP protocol is insecure. Use SFTP, SCP or FTPS instead.}}
$url = "telnet://anonymous@exemple.com"; // Noncompliant {{Using Telnet protocol is insecure. Use SSH instead.}}

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
$url = "http://[::1]"; // Compliant - proper bracketed IPv6 loopback
$url = "http://::1"; // Noncompliant
$url = "ftp://user@localhost"; // Compliant
$url = 123; // Compliant

$url = "http://"; // Compliant
$url = "ftp://"; // Compliant
$url = "telnet://"; // Compliant

// Cloud IMDS and internal network hosts
$url = "http://169.254.169.254"; // Compliant - link-local (AWS/Azure/GCP IMDS)
$url = "http://metadata.google.internal"; // Compliant - GCP cloud metadata
$url = "http://host.docker.internal"; // Compliant - Docker internal hostname
$url = "http://my-service.default.svc.cluster.local"; // Compliant - Kubernetes cluster-internal

// test.com is a real registered domain, not an RFC 6761 reserved TLD
$url = "http://test.com"; // Noncompliant
$url = "http://someSubdomain.test.com"; // Noncompliant
$url = "http://someUrl.com?url=test.com"; // Noncompliant
$url = "http://shortest.com"; // Noncompliant

// RFC 6761 reserved TLDs and IANA documentation domains
$url = "http://api.test"; // Compliant - RFC 6761 .test reserved TLD
$url = "http://example.net"; // Compliant - IANA documentation domain
$url = "http://example.org"; // Compliant - IANA documentation domain

$url = "http://xmlns.com"; // Compliant
$url = "http://someSubdomain.xmlns.com"; // Noncompliant
$url = "http://someUrl.com?url=xmlns.com"; // Noncompliant

$url = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"; // Compliant - OOXML namespace URI authority

$url = str_replace('http://', '', $foo);  // Compliant
$url = str_replace('http://', '', $foo) . $bar;  // Compliant
$url = 'http://' . $bar; // Noncompliant
$url = 'http://something'; // Noncompliant

// starts with http:// regardless of the empty authority
$url = "http:///www.php-fig.org/"; // Noncompliant

return;
