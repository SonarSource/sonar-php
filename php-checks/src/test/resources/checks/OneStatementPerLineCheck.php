<?php

doSomething(); doSomething();                   // NOK

if ($a) doSomething();                          // NOK

if ($a) {}                                      // OK

while ($a);                                     // OK

label: while ($a) {                             // OK
    goto label;
  }

$a = function () { return 1; };                 // OK

$a = call(function($a) { return 1; }, function($a) { return 1; });  // NOK

$a = function () { doSomething(); return 1; };  // NOK - more than one statement nested in anonymous function.

?>

html

<?php echo "Hello"; ?>                          // OK

html

<h1 a="<?php echo $a;?>" b="<?php echo $b;?>">  // OK
