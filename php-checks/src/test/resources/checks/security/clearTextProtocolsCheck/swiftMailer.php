<?php

$transport1 = (new Swift_SmtpTransport('XXX', 1234))
  ->setEncryption(null) // Noncompliant
;

$transport2 = (new Swift_SmtpTransport('XXX', 1234))
  ->setEncryption('tcp') // Noncompliant
;

$transport3 = (new Swift_SmtpTransport('XXX', 1234)); // Noncompliant



$transport1 = (new Swift_SmtpTransport('smtp.example.org', 1234))
  ->setEncryption('tls') // Compliant
;

$transport1 = (new Swift_SmtpTransport('smtp.example.org', 1234))
  ->setEncryption('ssl') // Compliant
;

$transport1 = (new Swift_SmtpTransport('smtp.example.org', 1234))
  ->setEncryption($encryption) // Compliant
;

$transport3 = (new Swift_SmtpTransport('localhost', 1234)); // Compliant
$transport4 = (new Swift_SmtpTransport('ssl://smtp.example.org', 1234)); // Compliant
$transport4 = (new Swift_SmtpTransport('tls://smtp.example.org', 1234)); // Compliant
