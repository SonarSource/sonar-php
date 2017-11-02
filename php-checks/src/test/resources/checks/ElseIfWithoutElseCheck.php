<?php

if ($a) {
} elseif ($a) {
} elseif ($a) {      // Noncompliant {{Add the missing "else" clause.}}
//^^^^^^
}

if ($a):
  elseif ($a):         // Noncompliant
//^^^^^^
endif;

if ($a) {
} else if ($a) {     // Noncompliant
//^^^^^^^
}

if ($a) {
} elseif ($a) {
} else if ($a) {     // Noncompliant
}

if ($a) {
} else if ($a) {
} elseif ($a) {      // Noncompliant
}

if (true) {
 } else if (true) {
 } else if (true) {  // Noncompliant
 }

if (true) {
 } else if (true) {
 } else if (true) {
 } elseif (true) {
 } else {            // OK
 }

if ($a) {            // OK
} elseif ($a) {
} else {
}

if ($a) {            // OK
}

if ($a) {            // OK
} else {
}

if ($a) {            // OK
} else {
  if ($a) {
  }
}
