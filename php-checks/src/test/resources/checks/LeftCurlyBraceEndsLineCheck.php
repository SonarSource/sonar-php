<?php

function f ()
{                                // NOK
}

if (true)                        // NOK
{
}

$a = function () { doSomething;  // NOK
};

function f() {                   // OK
}

$var->
{'user_' . $id};                 // OK

if (true) { doSomething(); }     // OK

$var->{'user_' . $id};

{                                // OK
  echo $a;
}

if (true) {  ?>                  // OK
html
<?php }

if (true) { ?> html <? }         // OK
