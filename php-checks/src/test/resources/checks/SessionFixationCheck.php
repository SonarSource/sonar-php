<?php

$test = 'test';

session_id($_GET['id']); // Noncompliant {{Make sure the session ID being set here is cryptographically secure and is not user-supplied.}}
session_id(geterateSessionId()); // Noncompliant {{Make sure the session ID being set here is cryptographically secure and is not user-supplied.}}
\session_id($$test); // Noncompliant {{Make sure the session ID being set here is cryptographically secure and is not user-supplied.}}
session_id(${'test'}); // Noncompliant {{Make sure the session ID being set here is cryptographically secure and is not user-supplied.}}
session_ID('test_' . $test); // Noncompliant {{Make sure the session ID being set here is cryptographically secure and is not user-supplied.}}

  session_id('test'); // Noncompliant {{Make sure the session ID being set here is cryptographically secure and is not user-supplied.}}
//^^^^^^^^^^^^^^^^^^

$id = session_id(); // OK
