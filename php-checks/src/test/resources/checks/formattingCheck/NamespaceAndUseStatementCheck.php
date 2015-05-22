<?php

use my\space\AnotherClass;  // NOK - uses are before namespace
use my\space\MyClass;       // NOK - no blank line after uses
namespace another\bar;      // NOK - no blank line after namespace
{
}


use my\space\MyClass;       // NOK - no blank line after use
{
}

use my\space\MyClass;       // NOK - no blank line after use
// use
{
}

namespace another\bar;      // NOK - no blank line after namespace
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
