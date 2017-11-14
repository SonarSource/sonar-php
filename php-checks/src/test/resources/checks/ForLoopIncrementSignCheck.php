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

for ($i = 0; (($length == 3) || ($this->x > $length)); $this->x++) { // Noncompliant
  //                             ^^^^^^^^^^^^^^^^^^>   ^^^^^^^^^^
}

for ($i = 0; $i < $length; $i++) {
}

for ($i = 0; $i > $length; $i--) {
}

for ($i = 0; $i > $length; /* coverage */) {
}

for ($i = 0; /* coverage */ ; $i--) {
}
