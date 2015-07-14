<?php

/*
Comment
block
*/
function f1($p) {
  // comment line
  if ($p) {
    return 1;
  }
  return 2;
}

function f2($p) {
}

class A {
  private function f() {
    if ($x) {
      print $x;
    }
  }
}

// commented out code
// $x = 1;
// $x = function1();

// duplication1
$x = f1() + f2() + f3() + f4() + f5() - f6();
$x = $x + $y * $z - $w;
while (f($x) < 10) {
  $x++;
}
$x = ($x + 1) * $y + f($z) / 2;
$x += f($y) + 1;
if ($x < $f1($y) + 1) {
  print f($x) + f2() + f3();
}
$x--;

// duplication2
$x = f1() + f2() + f3() + f4() + f5() - f6();
$x = $x + $y * $z - $w;
while (f($x) < 10) {
  $x++;
}
$x = ($x + 1) * $y + f($z) / 2;
$x += f($y) + 1;
if ($x < $f1($y) + 1) {
  print f($x) + f2() + f3();
}
$x--;


?>
