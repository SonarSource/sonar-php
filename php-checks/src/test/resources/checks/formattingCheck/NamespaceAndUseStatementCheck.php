<?php

  use my\space\AnotherClass;  // Noncompliant {{Move the use declarations after the namespace declarations.}}
//^^^
  use my\space\MyClass;       // Noncompliant {{Add a blank line after this "use" declaration.}}
//^^^
  namespace another\bar;      // Noncompliant {{Add a blank line after this "namespace another\bar" declaration.}}
//^^^^^^^^^
{
}

namespace {}      // Noncompliant {{Add a blank line after this "namespace" declaration.}}
{
}


use my\space\MyClass;       /* Noncompliant */ // no blank line after use
{
}

use my\space\MyClass;       /* Noncompliant */ // no blank line after use
// use
{
}

namespace another\bar;      /* Noncompliant */ // no blank line after namespace
// namespace
{
}

namespace                   // OK
{
  use x;                    // OK
}

namespace foo\bar;          // OK

use my\space\MyClass;       // OK

{
}
