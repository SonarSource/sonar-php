<?php

/**
 * Space after control structure keyword
 */
if($a) {           // NOK

} else if  ($b) {  // NOK

} else{            // NOK

}

try {              // OK

} catch (Exception $e) {  // OK

}

try
{                         // OK - on another line
} catch (Exception $e) {
}

/**
 * Space after ";" in for statement
 */

for ($i = 0;$i < 3;  $i++) {  // NOK
}

for ($i = 0; $i < 3; $i++) {  // OK
}

/**
 * Foreach spacing
 */

foreach ($a as  $array) {}          // NOK
foreach ($a as $map =>  $value) {}  // NOK
foreach ($a as  $map  =>$value) {}  // NOK
foreach ($a as $map => $value) {}   // OK

