<?php
$length = 2;

for ($i = 0; $i < $length;                    $i--) { // Noncompliant {{"$i" is decremented and will never reach "stop condition".}}
  //         ^^^^^^^^^^^^> {{Stop condition}} ^^^^
}

for ($i = 0; $i > $length; $i++) { // Noncompliant {{"$i" is incremented and will never reach "stop condition".}}
  //         ^^^^^^^^^^^^> ^^^^
}

for ($i = 0; $length == 3 || $i > $length; $i++) { // Noncompliant
  //                         ^^^^^^^^^^^^> ^^^^
}

for ($i = 0; $length == 3 || $i > $length; $i+= 5) { // Noncompliant
  //                         ^^^^^^^^^^^^> ^^^^^^
}

for ($i = 0; $length == 3 || $i > $length; $i+= +5) { // Noncompliant
  //                         ^^^^^^^^^^^^> ^^^^^^^
}

for ($i = 0; $length == 3 || $i > $length; $i+= 0) { // Noncompliant
  //                         ^^^^^^^^^^^^> ^^^^^^
}

for ($i = 0; $length == 3 || $i > $length; $i+= -5) {
}

for ($i = 0; $length == 3 || $i > $length; $i+= 1 + 1) { // false-negative
}

for ($i = 0; $length == 3 || $i < $length; $i-= -5) {

}

for ($i = 0; $length == 3 || $i > $length; $i-= -5) { // Noncompliant
  //                         ^^^^^^^^^^^^> ^^^^^^^
}

for ($i = 0; $length == 3 || $i < $length; $i-= +5) { // Noncompliant
  //                         ^^^^^^^^^^^^> ^^^^^^^
}

for ($i = 0; $length == 3 || $i < $length; $i-= 1 + 1) { // false-negative
}

for ($i = 0; (($length == 3) || ($this->x > $length)); $this->x++) { // Noncompliant
  //                             ^^^^^^^^^^^^^^^^^^>   ^^^^^^^^^^
}

for ($i = 0, $j = 0; $i < 10 || $j > 10; $i++, $j++) { // Noncompliant
  //                            ^^^^^^^>       ^^^^
}

for ($i = 0; $i > $length || $i < $length; $i = $i * $i, $i = "a string", $i = -($i), $i = +($i)) {
}

for ($i = 0; $i < $length; $i++) {
}

for ($i = 0; $i > $length; $i--) {
}

for ($i = 0; $i > $length; /* coverage */) {
}

for ($i = 0; /* coverage */ ; $i--) {
}
