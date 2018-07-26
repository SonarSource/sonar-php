<?php

$a = flush(); // Noncompliant {{Remove this use of the output from flush; flush doesn't return anything.}}
//   ^^^^^

$a = (flush()); // Noncompliant

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

