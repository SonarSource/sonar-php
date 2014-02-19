<?php

function f ()
{                                // NOK
}

if (true)                        // NOK
{
}

$a = function () { doSomething;  // NOK
};

$var->
{'user_' . $id};                 // OK

if (true) { doSomething(); }     // OK

$var->{'user_' . $id};

{                                // OK
  echo $a;
}

