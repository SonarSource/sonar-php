<?php

if ($condition1) {

} else if ($condition2) {  // Noncompliant {{Replace this "else if" keyword sequence by "elseif" keyword.}}
//^^^^^^^

} elseif ($condition3) {   // OK

} else {

}

if ($condition1):
else:                      // OK
 if ($condition2):
 endif;
endif;

if ($condition1):
else:                      // OK
 if ($condition2){}
endif;
