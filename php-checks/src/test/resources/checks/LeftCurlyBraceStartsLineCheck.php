<?php

function myMethod()
{                                // OK
  if(something)
  {                              // OK
    executeTask();
  } else {                       // NOK
    doSomethingElse();
  }
}

$a = function () {               // NOK
doSomething;};

$a = function () {doSomething;}; // OK
