<?php

function myMethod()
{                                // OK
  if(something)
  {                              // OK
    executeTask();
  } else {                       // NOK {{Move this open curly brace to the beginning of next line.}}
    doSomethingElse();
  }
}

$a = function () {               // NOK {{Move this open curly brace to the beginning of next line.}}
doSomething;};

$a = function () {doSomething;}; // OK

class A {                       // NOK {{Move this open curly brace to the beginning of next line.}}

  function foo(){               // NOK {{Move this open curly brace to the beginning of next line.}}
  }

  use SomeTrait {               // NOK {{Move this open curly brace to the beginning of next line.}}
  }

  use SomaTrait;
}

namespace {                      // NOK {{Move this open curly brace to the beginning of next line.}}
}

declare (a) {                    // NOK {{Move this open curly brace to the beginning of next line.}}
}

switch (a) {                     // NOK {{Move this open curly brace to the beginning of next line.}}
}

if (a)
{
} elseif (b) {                   // NOK {{Move this open curly brace to the beginning of next line.}}
}

for (i = 0; i < 10; i++) {       // NOK {{Move this open curly brace to the beginning of next line.}}
}

foreach ($a as $b) {             // NOK {{Move this open curly brace to the beginning of next line.}}
}

do {                             // NOK {{Move this open curly brace to the beginning of next line.}}
} while (true);

while (true) {                    // NOK {{Move this open curly brace to the beginning of next line.}}
}

try {                             // NOK {{Move this open curly brace to the beginning of next line.}}
} catch (Type $e) {               // NOK {{Move this open curly brace to the beginning of next line.}}
} finally {                       // NOK {{Move this open curly brace to the beginning of next line.}}
}