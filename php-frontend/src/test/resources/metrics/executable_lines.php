<p>
html is not considered as executable
</p>

<?php
// comment

class A {

  public function f() {
    foo(1,        // +1
        2,
        3);

    echo "hello"; // +1

    label1:
    for ($i = 1;  // +1
         $i < 1;
         $i++) {

      if (true) { // +1
        goto label1; // +1
        break;    // +1
      }

      yield 42;   // +1
      continue;   // +1
    }

    return foo(1, // +1
               2);
  }
}

static $a = 0;           // +1

while ($a) {             // +1
  throw new Exception(); // +1
}

try {                    // +1
  do {                   // +1

  } while ($a);

} catch (Exception $e) {
  switch (1) {           // +1
  }
}

declare(ticks=1) { // +1
  foo();           // +1
}

global $foo;       // +1
unset($i);         // +1

foreach ($arr as &$value) { // +1
    ; // +1
}
?>
