<?php

for(;condition;) {             // NOK {{Replace this "for" loop with a "while" loop.}}
}

for(;;) {                      // NOK
}

for (;condition;):             // NOK
endfor;

for($i = 0; condition;) {      // OK
}

for(; condition; i++) {        // OK
}

for($i = 0; condition; i++) {  // OK
}

for($i = 0;; i++) {            // OK
}

