<?php

if ($a == true) {  // NOK
}

if ($a != false) {  // NOK
}

call(true == $a);  // NOK

call(true != $a);  // NOK

if ($a != x) {     // OK
}

if ($a) {          // OK
}

if (!$a) {         // OK
}

if ($a.call()) {   // OK
}

