<?php

function nonCompliant() {

  if ($condition)
    firstActionInBlock();
  //^^^^^^^^^^^^^^^^^^^^^> {{Executed conditionally}}
    secondAction(); // Noncompliant {{This statement will not be executed conditionally; only the first statement of this 3-statement block will be. The rest will execute unconditionally.}}
  //^^^^^^^^^^^^^^^
    thirdAction();
  //^^^^^^^^^^^^^^< {{Always executed}}

  for ($i = 0; $i < 2; $i++)
    firstActionInBlock();
  //^^^^^^^^^^^^^^^^^^^^^> {{Executed in a loop}}
    secondAction(); // Noncompliant {{This statement will not be executed in a loop; only the first statement of this 3-statement block will be. The rest will execute only once.}}
  //^^^^^^^^^^^^^^^
    thirdAction();
  //^^^^^^^^^^^^^^< {{Executed once}}

  if ($condition) firstActionInBlock();

  if ($condition) firstActionInBlock();  secondAction();  // Noncompliant
  //              ^^^^^^^^^^^^^^^^^^^^^> ^^^^^^^^^^^^^^^

  if ($condition);  secondAction();  // Noncompliant
  //             ^> ^^^^^^^^^^^^^^^

  if ($condition) firstActionInBlock();
  secondAction();

  if ($condition) firstActionInBlock();
 secondAction();

  if ($condition) firstActionInBlock();
   secondAction(); // Noncompliant

  if ($condition) firstActionInBlock();
    secondAction();  // Noncompliant

  if ($condition)
    firstActionInBlock();
     secondAction(); // Noncompliant

  if ($condition)
    firstActionInBlock();
    secondAction(); // Noncompliant

  if ($condition)
    firstActionInBlock();
  secondAction();

  if ($condition)
  firstActionInBlock();
    secondAction(); // Noncompliant

  if ($condition)
  firstActionInBlock();
  secondAction(); // Noncompliant

  if ($condition)
  firstActionInBlock();
 secondAction();

  if ($condition)
  firstActionInBlock();

  secondAction();

  if ($condition) firstActionInBlock();
  secondAction();

  thierdAction();

  if ($condition)
    firstActionInBlock();



    secondAction();
    thirdAction();

  if ($condition)
  firstActionInBlock();

  secondAction();

  thirdAction();

  if ($condition) {
  } elseif ($condition)
    firstActionInBlock();
    secondAction();  // Noncompliant {{This statement will not be executed conditionally; only the first statement of this 2-statement block will be. The rest will execute unconditionally.}}
//  ^^^^^^^^^^^^^^^

  if ($condition)
    firstActionInBlock();
    secondAction();  // Noncompliant
  thirdAction();

  if ($condition) {
  } else if ($condition)
    firstActionInBlock();

    secondAction();  // Noncompliant

  if ($condition) {
  } else if ($condition) {
  } else
    firstActionInBlock();

    secondAction();  // Noncompliant

  if ($condition) {
    action();
  } else
    firstActionInBlock();
    secondAction();  // Noncompliant

  $str = null;
  for ($i = 0; $i < count($array); $i++)
    $str = $array[$i];
    doTheThing($str);  // Noncompliant {{This statement will not be executed in a loop; only the first statement of this 2-statement block will be. The rest will execute only once.}}

  while (true)
    firstActionInBlock();
    secondAction();  // Noncompliant

  $test = [1, 2];
  foreach ($test as $intValue)
    firstActionInBlock();
    // comment
    // bla bla bla
    secondAction();  // Noncompliant
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

if ($condition)
  if ($condition)
    firstActionInBlock();
    secondAction(); // Noncompliant

if ($condition)
  if ($condition)
    firstActionInBlock();
  secondAction(); // Noncompliant

if ($condition)
  if ($condition)
    firstActionInBlock();
secondAction();

?>
