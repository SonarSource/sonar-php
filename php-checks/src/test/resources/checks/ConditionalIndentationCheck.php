<?php

  $x = 7;
  $arr = array(1, 2);

  function doTheThing() { }

  function doTheOtherThing() { }

  if ($x > 0)  // Noncompliant {{Use curly braces or indentation to denote the code conditionally executed by this "if".}}
//^^
  doTheThing();
//^^^^^^^^^^<
  doTheOtherThing();

  if ($x > 0)
    doTheThing();
  doTheOtherThing();

  if ($x > 0) {
    doTheThing();
    doTheOtherThing();
  }

  if ($x > 0)
  {
    doTheThing();
    doTheOtherThing();
  }

  while ($x <= 10) // Noncompliant
  doTheThing();
  doTheOtherThing();

  while ($x <= 10)
    doTheThing();
  doTheOtherThing();

  while ($x <= 10) {
    doTheThing();
    doTheOtherThing();
  }

  while ($x <= 10): // Noncompliant
  doTheThing();
  doTheOtherThing();
  endwhile;

  while ($x <= 10):
    doTheThing();
    doTheOtherThing();
  endwhile;

  if ($x == 5): // Noncompliant
  doTheThing();
  doTheOtherThing();
  elseif ($x == 6): // Noncompliant
  doTheThing();
  doTheOtherThing();
  else: // Noncompliant
  doTheThing();
  doTheOtherThing();
  endif;

  if ($x == 5):
    doTheThing();
    doTheOtherThing();
  elseif ($x == 6):
    doTheThing();
    doTheOtherThing();
  else:
    doTheThing();
    doTheOtherThing();
  endif;

  if ($x == 5):
  elseif ($x == 6):
  else:
  endif;

  foreach ($arr as &$value) // Noncompliant
  doTheThing();
  doTheOtherThing();

  foreach ($arr as &$value)
    doTheThing();
  doTheOtherThing();

  foreach ($arr as &$value) {
    doTheThing();
    doTheOtherThing();
  }

  foreach ($arr as &$value): // Noncompliant
  doTheThing();
  doTheOtherThing();
  endforeach;

  foreach ($arr as &$value):
    doTheThing();
    doTheOtherThing();
  endforeach;

  for ($x = 1; $x <= 10; $x++) // Noncompliant
  doTheThing();
  doTheOtherThing();

  for ($x = 1; $x <= 10; $x++)
    doTheThing();
  doTheOtherThing();

  for ($x = 1; $x <= 10; $x++) {
    doTheThing();
    doTheOtherThing();
  }

  for ($x = 1; $x <= 10; $x++): // Noncompliant
  doTheThing();
  doTheOtherThing();
  endfor;

  for ($x = 1; $x <= 10; $x++):
    doTheThing();
    doTheOtherThing();
  endfor;
