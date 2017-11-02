<?php

doSomething(); doSomething();                   // Noncompliant {{2 statements were found on this line. Reformat the code to have only one statement per line.}}

if ($a) doSomething();                          // Noncompliant

if ($a) {}                                      // OK

while ($a);                                     // OK

label: while ($a) {                             // OK
    goto label;
  }

$a = function () { return 1; };                 // OK

$a = call(function($a) { return 1; }, function($a) { return 1; });  // Noncompliant {{3 statements were found on this line. Reformat the code to have only one statement per line.}}

$a = function () { doSomething(); return 1; };  /* Noncompliant */ // more than one statement nested in anonymous function.


if (true) {

} else if (true) {                                  // OK

}

if (true) {

} elseif (true) {                                  // OK

}

switch (a) {
  case 1 : echo 1;                              // OK
  default : echo 2;
}

?>

html

<?php echo "Hello"; ?>                          // OK

html

<h1 a="<?php echo $a;?>" b="<?php echo $b;?>">  // OK

<h1 a="<?php echo $a?>" b="<?php echo $b?>">  // OK

<h1 a="<?php echo $a?>" b="<?php echo $b;?>">  // OK



