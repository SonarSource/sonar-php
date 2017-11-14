<?php

function foo() {  // NOSONAR this is SO wrong
  // Noncompliant@-1 {{Is //NOSONAR used to exclude false-positive or to hide real quality flaw ?}}
  // nos0nar is in the issue message... causing another issue on line #4
} // Noncompliant@-2 

$bar = null;  # NOSONAR just stop raising issues
// Noncompliant@-1

/* This is a multi line comment
   with a NOSONAR in the middle
   yet another line of comment */
echo "hello"; // Noncompliant@-2
