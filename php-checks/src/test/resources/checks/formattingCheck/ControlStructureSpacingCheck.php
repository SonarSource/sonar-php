<?php

/**
 * Space after control structure keyword
 */
  if($a) {           // Noncompliant {{Put one space between this "if" keyword and the opening parenthesis.}}
//^^

} else if  ($b) {  // Noncompliant {{Put only one space between this "if" keyword and the opening parenthesis.}}
//     ^^

} else{            // Noncompliant {{Put one space between this "else" keyword and the opening curly brace.}}
//^^^^

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

for ($i = 0;$i < 3;  $i++) {  // Noncompliant {{Put exactly one space after each ";" character in the "for" statement.}}
//         ^
}

for ($i = 0; $i < 3; $i++) {  // OK
}

/**
 * Foreach spacing
 */

  foreach ($a as  $array) {}          // Noncompliant {{Put exactly one space after and before "as" in "foreach" statement.}}
//            ^^
foreach ($a as $map =>  $value) {}  // Noncompliant {{Put exactly one space after and before "=>" in "foreach" statement.}}
//                  ^^
foreach ($a as  $map  =>$value) {}  // Noncompliant {{Put exactly one space after and before "as" and "=>" in "foreach" statement.}}
//          ^^        ^^<
foreach ($a as $map => $value) {}   // OK

foreach ($tokens as $token) {
    array(
      'listener'   => $this->listeners[$listenerClass],
      'class'      => $listenerClass,
      'tokenizers' => $tokenizers,
     );

}

foreach (
    $varXXXXXXXXXXXXXXXXXXXXX_long_variable_XXXXXXXXXXXXXXXXXXXXX
    as $keyXXXXXXXXXXXXXXXXXXXXX_long_variable_XXXXXXXXXXXXXXXXXXXXX
=> $valueXXXXXXXXXXXXXXXXXXXXX_long_variable_XXXXXXXXXXXXXXXXXXXXX
) {
}

foreach (
    $varXXXXXXXXXXXXXXXXXXXXX_long_variable_XXXXXXXXXXXXXXXXXXXXX
    as  $keyXXXXXXXXXXXXXXXXXXXXX_long_variable_XXXXXXXXXXXXXXXXXXXXX // Noncompliant
    => $valueXXXXXXXXXXXXXXXXXXXXX_long_variable_XXXXXXXXXXXXXXXXXXXXX
) {
}

foreach (
    $varXXXXXXXXXXXXXXXXXXXXX_long_variable_XXXXXXXXXXXXXXXXXXXXX
    as $keyXXXXXXXXXXXXXXXXXXXXX_long_variable_XXXXXXXXXXXXXXXXXXXXX
=>  $valueXXXXXXXXXXXXXXXXXXXXX_long_variable_XXXXXXXXXXXXXXXXXXXXX  // Noncompliant
) {
}

foreach (
    $x as $y
=> $z
) {
}

foreach ( // side-effect of allowing foreach arguments to be on separate lines
    $x
              as $y
                         => $z
) {
}

// case-sensitivity of keywords

If ($x) {}
If($x) {} // Noncompliant
If ($x) {} Else {}
If ($x) {} Else{} // Noncompliant
For ($i = 0; $i < 3; $i++) {}
For ($i = 0;$i < 3; $i++) {} // Noncompliant
