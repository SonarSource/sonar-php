<?php

$transport1 = (new Swift_SmtpTransport('XXX', 1234))->setEncryption(null); // Noncompliant

$transport2 = (new Swift_SmtpTransport('XXX', 1234))->setEncryption('tcp'); // Noncompliant

$transport3 = (new Swift_SmtpTransport('XXX', 1234)); // Noncompliant

$transport11 = (new Swift_SmtpTransport('localhost', 1234))->setHost('smtp.example.org'); // Noncompliant

$transport12 = (new Swift_SmtpTransport('smtp.example.org', 1234, 'tls'))->setEncryption('tcp'); // Noncompliant

$transport13 = (new Swift_SmtpTransport('localhost', 1234))->setEncryption(null)->setHost('smtp.example.org'); // Noncompliant
$transport14 = (new Swift_SmtpTransport('smtp.example.org', 1234))->setEncryption(null)->setHost('localhost'); // Compliant
$transport15 = (new Swift_SmtpTransport('smtp.example.org', 1234))->setEncryption(null)->setHost('tls://smtp.example.org'); // Compliant

$transport15 = (new Swift_SmtpTransport('smtp.example.org', 1234))->setEncryption('tls')->setEncryption(null); // Noncompliant
$transport16 = (new Swift_SmtpTransport('smtp.example.org', 1234))->setEncryption(null)->setEncryption('tls'); // Compliant

$transport16 = (new Swift_SmtpTransport()); // Compliant - default host is localhost
$transport17 = (new Swift_SmtpTransport())->setHost('smtp.example.org'); // Noncompliant
$transport18 = (new Swift_SmtpTransport())->setHost('smtp.example.org')->setEncryption('tls'); // Compliant

$transport19 = (new Swift_SmtpTransport('smtp.example.org')); // Noncompliant
$transport20 = (new Swift_SmtpTransport('smtp.example.org', encryption: 'tls')); // Compliant
$transport21 = (new Swift_SmtpTransport('smtp.example.org', encryption: 'tcp')); // Noncompliant
$transport22 = (new Swift_SmtpTransport('localhost', encryption: 'tcp')); // Compliant
$transport23 = (new Swift_SmtpTransport(encryption: 'tcp', host: 'localhost')); // Compliant
$transport24 = (new Swift_SmtpTransport(encryption: 'tcp', host: 'smtp.example.org')); // Noncompliant

$transport4 = (new Swift_SmtpTransport('smtp.example.org', 1234))->setEncryption('tls'); // Compliant
$transport5 = (new Swift_SmtpTransport('smtp.example.org', 1234))->setEncryption('ssl'); // Compliant
$transport6 = (new Swift_SmtpTransport('smtp.example.org', 1234))->setEncryption($encryption); // Compliant

$transport7 = (new Swift_SmtpTransport('localhost', 1234)); // Compliant
$transport8 = (new Swift_SmtpTransport('ssl://smtp.example.org', 1234)); // Compliant
$transport9 = (new Swift_SmtpTransport('tls://smtp.example.org', 1234)); // Compliant
$transport27 = (new Swift_SmtpTransport($host, 1234)); // Compliant

$transport10 = new Swift_SmtpTransport('smtp.example.org', 1234); // Compliant
$transport10->setHost("localhost");

$transport25 = (new Swift_SmtpTransport('XXX', 1234))->$foo("bar"); // Compliant - dynamic method call
$transport26 = (new Swift_SmtpTransport('XXX', 1234)); // Compliant - dynamic method call
$transport26->$foo("bar");
$transport26->notRelevant("tcp");
$transport26->notRelevant2();
