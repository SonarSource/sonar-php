<?php

// IPv4-like strings

const serverAddress = "1.20.33.145";  // Noncompliant {{Make sure using this hardcoded IP address is safe here.}}
//                    ^^^^^^^^^^^^^
const nonRelatedValue = 42;

class A {
  const constFieldNameWithPasswordInIt = "8.8.8.8"; // Noncompliant
  const otherConstFieldName = "http://1.2.3.4:8080/foo"; // Noncompliant
}

function foo($socket) {
  static $otherStaticVariableName = "xxx";
  socket_connect($socket, '1.1.1.1', 23); // Noncompliant
}

const addressList = "1.2.3.4,5.6.7.8"; // Noncompliant
const addressWithTrailingSpace = "1.2.3.4 "; // Noncompliant
const addressWithLeadingSpace = " 1.2.3.4"; // Compliant - beginning is stricter than ending
const addressWithExtraDot = "1.2.3.4."; // Compliant

const localhost = "127.0.0.1"; // Compliant - Loopback address
const localhostUrl = "ftp://127.0.0.1:22/bla"; // Compliant - exception for localhost
const notIPAddress = "1.20.33.345"; // Compliant - segment > 255
const notRouteable = "0.0.0.0"; // Compliant

const versionNumber = "3.0.1.2"; // Noncompliant
                                 // Cannot distinguish from an IP address :(
const buildNumber = "145.33.7.12c0"; // Compliant

// IPv6 like strings

$serverAddress = "[2003:db8:a0b:12f0::1]:21";                   // Noncompliant
//               ^^^^^^^^^^^^^^^^^^^^^^^^^^^
$serverAddress = "2003:db8:a0b:12f0::1";                        // Noncompliant
$serverAddress = "2003:db8:a0b:12f0:12:1";                      // Compliant
$serverAddress = "2003:0db8:0a0b:12f0:0000:0000:0000:0001";     // Noncompliant
$serverAddress = "::1";                                         // Compliant - Loopback address
$serverAddress = "[::1]";                                       // Compliant - Loopback address
$serverAddress = "0:0:0:0:0:0:0:1";                             // Compliant - Loopback address
$serverAddress = "::2";                                         // Noncompliant
$serverAddress = "2003:db8:3:4::192.0.2.33";                    // Noncompliant
$serverAddress = "1234:::1";                                    // Compliant - no IPv6
$serverAddress = '0:1234:dc0:41:216:3eff:fe67:3e01';            // Noncompliant

$url = "http://[2003:db8:a0b:12f0::1]/index.html";              // Noncompliant
//     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
// 2001:db8::/32 is for documentation
$url = "http://[2001:db8:a0b:12f0::1]/index.html";              // Compliant

$randomText = "sdfsdfsdf asda sd::2";
$randomText = "sdfsdfsdf asda 8.8.8.8";
$class = $string . '::' . $string;
$class = self::class.'::controller';

// local IPv4-mapped IPv6 address
$serverAddress = "::ffff:0:127.0.0.1";
$serverAddress = "::FFFF:0:127.0.0.1";
$serverAddress = "::ffff:0000:127.0.0.1";
$serverAddress = "::ffff:127.0.0.1";
$serverAddress = "::ffff:0:127.128.128.128";
$serverAddress = "::ffff:0:127.255.255.255";
$serverAddress = "::ffff:0:128.0.0.1";                           // Noncompliant
$serverAddress = "::ffff:200.0.0.1";                             // Noncompliant
//               ^^^^^^^^^^^^^^^^^^

# RFC 3849
$serverAddress = "192.0.2.1";
$serverAddress = "198.51.100.1";
$serverAddress = "203.0.113.1";
$serverAddress = "2001:db8:0a0b:12f0:0000:0000:0000:0001";
$serverAddress = "2001:0db8:0a0b::::0000:0001";

// Broadcast addresses should be ignored
const broadCastIpV4 = "255.255.255.255";

$ip = "3fff::1"; // Compliant
$ip = "::"; // Compliant
$ip = "ff02::1"; // Noncompliant
$ip = "10.0.0.1"; // Noncompliant

// Extract the complete host, including dotted IPv6 tails, brackets, zones, and ports.
$ip = "::1.2.3.4"; // Noncompliant
$ip = "//[2003::1%eth0]:8080/path?query#fragment"; // Noncompliant

// Don't raise on strings that are not IPs
$ip = "8.8.8.8-foo"; // Compliant

// Classify the entire CIDR range.
$ip = "127.0.0.1/7"; // Noncompliant
$ip = "127.0.0.1/8"; // Compliant
$ip = "0.0.0.0/0"; // Compliant
$ip = "::/0"; // Compliant

namespace VersionComparison {
  use function version_compare as compare_versions;

  version_compare('1.2.3.4', '2.3.4.5', '8.8.8.8'); // Compliant
  compare_versions(version2: '2.3.4.5', version1: ('1.2.3.4')); // Compliant
  version_compare('1.2.3.4', ...$versions); // Compliant
  version_compare('1.2.3.4' . '/endpoint', $version); // Noncompliant
  version_compare(parse('1.2.3.4'), $version); // Noncompliant
  $obj->version_compare('1.2.3.4', $version); // Noncompliant
  // FN: IPv6 comparison data is unlikely; we suppress all literals passed directly to version_compare.
  version_compare('2003:db8:a0b:12f0::1', $version); // Compliant
}

namespace ClassNameSuffix {
  use function get_class as class_name;

  get_class($obj) . '::add'; // Compliant
  (class_name($obj)) . (('::feed')); // Compliant
  get_class(new Foo(version_compare('1.2.3.4', $version))) . '::add'; // Compliant

  // FN: These suffixes do not name class members; the broad suffix exemption accepts them for simplicity.
  // There is no reason anyone would actually use IPs here.
  get_class($obj) . '::add/path'; // Compliant
  get_class($obj) . '::2'; // Compliant
  get_class($obj) . '8.8.8.8'; // Compliant

  get_class($obj) . '::add' . '/path'; // Compliant
  $prefix . ((get_class($obj) . '::add')); // Compliant
  get_class($obj) . '::add' . '8.8.8.8'; // Noncompliant
  //                          ^^^^^^^^^
  $obj->get_class() . '::add'; // Noncompliant
  get_class(connect('8.8.8.8')) . '::add'; // Noncompliant
  //                ^^^^^^^^^

  get_class($obj) . some_wrapper('::add'); // Noncompliant

  $unknown = '::add'; // Noncompliant
}
