<?php

use my\space\AnotherClass;  // NOK - uses are before namespace
use my\space\MyClass;       // NOK - no blank line after uses
namespace another\bar;      // NOK - no blank line after namespace
{
}


use my\space\MyClass;       // NOK - no blank line after use
{

}


namespace foo\bar;          // OK

use my\space\MyClass;       // OK

{
}


