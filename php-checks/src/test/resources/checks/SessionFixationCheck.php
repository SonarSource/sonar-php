<?php

$test = 'test';

session_id($_GET['id']); // Noncompliant {{Make sure the session ID being set is cryptographically secure and is not user-supplied.}}
session_id(generateSessionId()); // Noncompliant
\session_id($$test); // Noncompliant
session_id(${'test'}); // Noncompliant
session_ID('test_' . $test); // Noncompliant

  session_id('test'); // Noncompliant
//^^^^^^^^^^^^^^^^^^

// OK
$id = session_id();
$myObj->session_id($_GET['id']);
session_regenerate_id();

// safe but we report hotspot issue
$sessionId = bin2hex(random_bytes(16));
session_id($sessionId); // Noncompliant
