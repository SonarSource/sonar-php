<?php

function nonCompliant() {

  if ($condition) {
  } elseif ($condition)
    firstActionInBlock();
    secondAction();  // NOK {{Only the first line of this 2-line block will be executed conditionally. The rest will execute unconditionally.}}
//  ^^^^^^^^^^^^^^^

  if ($condition)
    firstActionInBlock();
    secondAction();  // NOK {{Only the first line of this 2-line block will be executed conditionally. The rest will execute unconditionally.}}
  thirdAction();

  if ($condition) {
  } else if ($condition)
    firstActionInBlock();

    secondAction();  // NOK {{Only the first line of this 3-line block will be executed conditionally. The rest will execute unconditionally.}}

  if ($condition) {
  } else if ($condition) {
  } else
    firstActionInBlock();

    secondAction();  // NOK {{Only the first line of this 3-line block will be executed conditionally. The rest will execute unconditionally.}}

  if ($condition) {
    action();
  } else
    firstActionInBlock();
    secondAction();  // NOK {{Only the first line of this 2-line block will be executed conditionally. The rest will execute unconditionally.}}

  $str = null;
  for ($i = 0; $i < count($array); $i++)
    $str = $array[$i];
    doTheThing($str);  // NOK {{Only the first line of this 2-line block will be executed in a loop. The rest will execute only once.}}

  while (true)
    firstActionInBlock();
    secondAction();  // NOK {{Only the first line of this 2-line block will be executed in a loop. The rest will execute only once.}}

  $test = [1, 2];
  foreach ($test as $intValue)
    firstActionInBlock();
    // comment
    // bla bla bla
    secondAction();  // NOK {{Only the first line of this 4-line block will be executed in a loop. The rest will execute only once.}}
}




function compliant() {

  if ($condition) :
    firstActionInBlock();
    secondActionInBlock();
  endif;

  if ($condition)
    action();
  $outerAction;

  if ($condition) {
    firstActionInBlock();
    secondAction();
  } else if ($condition) {
  } else {
    other();
  }
  thirdAction();

  $str = null;
  for ($i = 0; $i < count($array); $i++) {
    $str = $array[$i];
    doTheThing($str);
  }

  if ($condition)
  {
    action();
  }
  $outerAction();
}

if ($condition)
doSomething();

?>
