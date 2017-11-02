<?php

  if ($a) {       // Noncompliant {{Replace this "if-then-else" statement by a single "return" statement.}}
//^^^^^^^
  return true;
} else {
  return false;
}

if ($a) {       // Noncompliant
  return false;
} else {
  return true;
}

if ($a)         // Noncompliant
  return true;
else
  return false;

if ($a):         // Noncompliant
  return true;
else:
  return false;
endif;

if ($a):         // OK
else:
  return false;
endif;


if ($a):         // OK
  $b;
  return true;
else:
  return false;
endif;

if ($a)         // OK
  return true;
elseif ($a)
  return false;
else
  return true;

if ($a) {       // OK
  return foo;
} else {
  return false;
}

if ($a) {       // OK
  return true;
} else {
  return foo;
}

if ($a) {       // OK
  doSomething();
} else {
  return true;
}

if ($a) {       // OK
  doSomething();
  return true;
} else {
  return false;
}

if ($a) {       // OK
  return;
} else {
  return true;
}

if ($a) {       // OK
  return true;
}

if ($a) {       // OK
  return foo(true);
} else {
  return foo(false);
}

if ($a) {       // OK
  $b;
} else {
  return false;
}

if ($a) {       // OK
} else {
  return false;
}
