<?php

function redundantJump($x) {
  if ($x == 1) {
    echo "x == 1";
    return; //  Noncompliant {{Remove this redundant jump.}}
//  ^^^^^^^
  }
}

function redundantJump1($condition1, $condition2) {
  while ($condition1) {
    if ($condition2) {
      continue; //  Noncompliant
    } else {
      echo "else";
    }
  }
}

function redundantJump2($b) {
  for ($i = 0; $i < 10; $i++) {
    continue; //  Noncompliant
  }
  if ($b) {
    echo "b";
    return; //  Noncompliant
  }
}

function compliant1($b) {
  for ($i = 0; $i < 10; $i++) {
    break;
  }
  if ($b) {
    echo "b";
    return;
  }
  echo "useful";
}


function return_in_non_void_method() {
  foo();
  return 42;
}


function switch_statements($x) {
  switch ($x) {
    case 0:
      foo();
      break;
    default:
  }
  foo();
  switch ($x) {
    case 0:
      foo();
      return;
    case 1:
      bar();
      return;
  }
  switch ($x) {
    case 0:
      foo();
      return;
    default:
      bar();
      return;
  }
}

function continue_levels() {
  for ($i = 0; $i < 10; $i++) {
    for ($j = 0; $j < 10; $j++) {
      continue 2;
    }
  }
}

function go_to() {
  foo();
  goto MyLabel; // Noncompliant
  MyLabel:
  bar();

  goto MyLabel; // OK
}

function inline_html() {
  if ($cond) {
    return; // not redundant, as inline html will be inserted
  }

  ?>
  		...
  <?php
}

function try_catch() {

  while ($cond) {
    dosmth();
    continue; // Noncompliant
  }

  try {
    foo();
    return;
  } catch (Exc $e) {
    bar();
  }
  after();
}

function fp_loop_jump() {
    while ($cond) {
        try {
            bar();
            continue;
        } finally {
            qix();
        }
    }
}
