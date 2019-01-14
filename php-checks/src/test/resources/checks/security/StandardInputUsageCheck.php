<?php

// Any reference to STDIN is Questionable
$varstdin = STDIN; // Noncompliant {{Make sure that reading the standard input is safe here.}}
//          ^^^^^
stream_get_line(STDIN, 40); // Noncompliant
//              ^^^^^
stream_copy_to_stream(STDIN, STDOUT); // Noncompliant
//                    ^^^^^

// Except those references as they can't create an injection vulnerability.
ftruncate(STDIN, 5); // OK
ftell(STDIN); // OK
feof(STDIN); // OK
fseek(STDIN, 5); // OK
fclose(STDIN); // OK


// STDIN can also be referenced like this
$mystdin = 'php://stdin'; // Noncompliant
//         ^^^^^^^^^^^^^

file_get_contents('php://stdin'); // Noncompliant
readfile("php://stdin"); // Noncompliant
//       ^^^^^^^^^^^^^
ReadFile("php://stdin"); // Noncompliant

$input = fopen('php://stdin', 'r'); // Noncompliant
//             ^^^^^^^^^^^^^
fclose($input); // OK

stream_set_blocking(STDIN, 0);
$stat  = fstat(STDIN);
$isTTY = (posix_isatty(STDIN) === true);

function getStdIn() {
  return STDIN; // Noncompliant
}

// coverage
$unknown(STDIN); // Noncompliant
if ($a == STDIN || !STDIN) {
}
$a->STDIN();
