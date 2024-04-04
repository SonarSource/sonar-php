<?php

$a = flush(); // Noncompliant {{Remove this use of the output from "flush"; "flush" doesn't return anything.}}
//   ^^^^^

$a = (Flush()); // Noncompliant

class A {
  public $field1 = usleep(12); // Noncompliant
}

function gen_one_to_three() {
  for ($i = 1; $i <= 3; $i++) {
    yield var_dump($i); // Noncompliant
  }
}

function func2($a, $b) {}

func2($a, var_dump($a)); // Noncompliant

$array = array("foo" => "bar", "bar" => socket_close ($socket)); // Noncompliant
$array = array("foo" => "bar", socket_close ($socket) => "bar"); // Noncompliant

$a = flush() ? 1 : 2; // Noncompliant
$a = $b ? flush() : 2; // Noncompliant
$a = $b ? 1 : flush(); // Noncompliant

switch (flush()) { // Noncompliant
  case 1 : break;
  case flush() : break; // Noncompliant
}

while (flush()) { // Noncompliant
}

do {
} while (flush()); // Noncompliant

if (flush()) { // Noncompliant
} elseif (flush()) { // Noncompliant
} else {
}

for ($i = 1; flush(); $i++) { // Noncompliant
  echo $i;
}

for ($i = 1; flush(), $i < 5; $i++) {
  echo $i;
}

for ($i = 1; ; flush(), $i++) {
  echo $i;
}

for ($i = 1; $i < 5, flush(); $i++) { // Noncompliant
  echo $i;
}

throw flush(); // Noncompliant
throw new Exception(flush()); // Noncompliant

function func3() {
  return flush(); // Noncompliant
}

$bar = (boolean) flush(); // Noncompliant

$a = -flush(); // Noncompliant

$a = 2 + flush(); // Noncompliant

$a = $array[flush()]; // Noncompliant
$a = flush()[2]; // Noncompliant

flush()->clear(); // Noncompliant

@closedir($v_folder_handler);

$a = MongoLog::setLevel(MongoLog::ALL); // Noncompliant

$converter = new UConverter();
$a = $converter->setSourceEncoding("UTF8"); // SONARPHP-739 False-negative due to lack of semantic on function call

$a = hex2bin("AF");

$header = new Header($preseller);

exit(0) || $foo; // Noncompliant
exit(0) && $foo; // Noncompliant
exit(0) or $foo; // Noncompliant
exit(0) and $foo; // Noncompliant
$foo || exit('There was an error');
$foo && exit('There was an error');
$foo or exit('There was an error');
$foo and exit('There was an error');
fun($foo and exit('There was an error')); // Noncompliant

exit(0) ? 0 : 1; // Noncompliant
$foo ? exit(0) : 42;
$foo ? 42 : exit(0);

function foo() {}
$foo = foo(); // Noncompliant

class Foo {
  public static function a() {}
//                       ^> {{Function definition.}}
  public function b() {
    $a = $this->a(); // Noncompliant {{Remove this use of the output from "Foo::a"; "Foo::a" doesn't return anything.}}
//       ^^^^^^^^
    $a = static::a(); // Noncompliant
  }
}

$foo = Foo::a(); // Noncompliant


abstract class B {
  public function function1() {
    $a = $this->function2(); // OK
    $a = $this->function3(); // Noncompliant {{Remove this use of the output from "B::function3"; "B::function3" doesn't return anything.}}
  }

  abstract protected function function2() {}

  protected function function3 () {}
}

class C extends B {
  protected function function2() {
    $foo = $this->function3(); // Noncompliant
    return $foo;
  }
}

function function4() {
  yield "Word";
}
$o = function4(); // OK

trait Authorizable
{
  public function authorizeToView(Request $request)
  {
    return $this->authorizeTo($request, 'view') && $this->authorizeToViewAny($request); // Noncompliant {{Remove this use of the output from "Authorizable::authorizeTo"; "Authorizable::authorizeTo" doesn't return anything.}}
  }

  public function authorizeTo(Request $request, $ability)
  {
    throw_unless($this->authorizedTo($request, $ability), AuthorizationException::class);
  }
}

class MatchCase {
  public function X($input) {
    match ($input) {
      1 => voidFunction(1),
      default => voidFunction(2)
    };

    $value = match ($input) {
      1 => voidFunction(1), // Noncompliant
      default => voidFunction(2) // Noncompliant
    };
  }
}

class ArrowFunctions {
  public function X() {
    $func = fn ($param) => voidFunction($param);
  }
}

function voidFunction($param) {
  echo $param;
}

abstract class AbstractTest {
  protected static function function1(): String {
    throw new BadMethodCallException('This method must be implemented by subclasses.');
  }

  protected static function function2(): void {
    throw new BadMethodCallException('This method must be implemented by subclasses.');
  }

  protected static function function3() {
    throw new BadMethodCallException('This method must be implemented by subclasses.');
  }

  protected static function caller() {
    $a = static::function1(); // OK
    $a = static::function2(); // Noncompliant
    $a = static::function3(); // Noncompliant
  }
}
