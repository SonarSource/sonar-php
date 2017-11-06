<?php
  if ($x == 1) {
  } if ($x == 1) { // Noncompliant {{Move this "if" to a new line or add the missing "else".}}
//  ^^
  }

  if ($x == 1) {
  } else {
  } if ($x == 1) { // Noncompliant {{Move this "if" to a new line or add the missing "else".}}
//  ^^
  }

  if ($x == 1) {
  } elseif ($x == 2) {
  } if ($x == 1) { // Noncompliant {{Move this "if" to a new line or add the missing "else".}}
//  ^^
  }

  if ($x == 1)
    doSomething(); if ($x == 1) { } // Compliant
