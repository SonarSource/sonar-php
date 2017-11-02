<?php

function myMethod()
{                                // OK
  if(something)
  {                              // OK
    executeTask();
  } else {                       // Noncompliant {{Move this open curly brace to the beginning of next line.}}
//       ^
    doSomethingElse();
  }
}

$a = function () {               // Noncompliant {{Move this open curly brace to the beginning of next line.}}
doSomething;};

$a = function () {doSomething;}; // OK

class A {                       // Noncompliant {{Move this open curly brace to the beginning of next line.}}

  function foo(){               // Noncompliant {{Move this open curly brace to the beginning of next line.}}
  }

  use SomeTrait {               // Noncompliant {{Move this open curly brace to the beginning of next line.}}
  }

  use SomaTrait;
}

namespace {                      // Noncompliant {{Move this open curly brace to the beginning of next line.}}
}

declare (a) {                    // Noncompliant {{Move this open curly brace to the beginning of next line.}}
}

switch (a) {                     // Noncompliant {{Move this open curly brace to the beginning of next line.}}
}

if (a)
{
} elseif (b) {                   // Noncompliant {{Move this open curly brace to the beginning of next line.}}
}

for (i = 0; i < 10; i++) {       // Noncompliant {{Move this open curly brace to the beginning of next line.}}
}

foreach ($a as $b) {             // Noncompliant {{Move this open curly brace to the beginning of next line.}}
}

do {                             // Noncompliant {{Move this open curly brace to the beginning of next line.}}
} while (true);

while (true) {                    // Noncompliant {{Move this open curly brace to the beginning of next line.}}
}

try {                             // Noncompliant {{Move this open curly brace to the beginning of next line.}}
} catch (Type $e) {               // Noncompliant {{Move this open curly brace to the beginning of next line.}}
} finally {                       // Noncompliant {{Move this open curly brace to the beginning of next line.}}
}

$x = new class {                       // Noncompliant {{Move this open curly brace to the beginning of next line.}}

  function foo(){               // Noncompliant {{Move this open curly brace to the beginning of next line.}}
  }

  use SomeTrait {               // Noncompliant {{Move this open curly brace to the beginning of next line.}}
  }

  use SomaTrait;
};
