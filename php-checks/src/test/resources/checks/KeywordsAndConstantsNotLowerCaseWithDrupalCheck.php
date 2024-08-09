<?php
use Drupal\something;

require_once "foo.php";   // OK

  include_ONCE "bar.php";   // Noncompliant {{Write this "include_ONCE" keyword in lower case.}}
//^^^^^^^^^^^^

  ECHO 'Hello World';       // Noncompliant {{Write this "ECHO" keyword in lower case.}}
//^^^^

  DIE('Error'); // Noncompliant
//^^^

  IF ($x) {} // Noncompliant
//^^

$a = null;                // Noncompliant {{Write this "null" constant in upper case.}}
//   ^^^^

if ($a == True) {         // Noncompliant {{Write this "True" constant in upper case.}}

} elseif ($a == FALSE) {  // OK

}

match($a);
match ($a) {$b=>42};
MATCH ($a) {default=>1}; // Noncompliant
MATCH(MATCH(1){1=>2}); //  Noncompliant
   // ^^^^^
MATCH($args); 

__HALT_COMPILER(); // Noncompliant

class X1 {
  private function foo () { STATIC::x(); } // Noncompliant
}


$obj = NEW C; // Noncompliant
//     ^^^
echo MyClass::NEW;
echo $obj->NEW;
class C {
  const NEW = 42;
}
class WithTraits {
    use A, B {
        B::NEW insteadof A;
        B::foo as NEW;
    }
}
