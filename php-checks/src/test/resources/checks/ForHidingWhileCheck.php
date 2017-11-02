<?php

  for(;condition;) {             // Noncompliant {{Replace this "for" loop with a "while" loop.}}
//^^^
}

for(;;) {                      // Noncompliant
}

for (;condition;):             // Noncompliant
endfor;

for($i = 0; condition;) {      // OK
}

for(; condition; i++) {        // OK
}

for($i = 0; condition; i++) {  // OK
}

for($i = 0;; i++) {            // OK
}

