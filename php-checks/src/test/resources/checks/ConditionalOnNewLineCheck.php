<?php
  if ($x) {
  }  if ($x) { // Noncompliant {{Move this "if" to a new line or add the missing "else".}}
//^> ^^
  }

  if ($x) {
  } else {
  }  if ($x) { // Noncompliant {{Move this "if" to a new line or add the missing "else".}}
//^> ^^
  }

  if ($x) {
  } elseif ($y) {
  }  if ($x) { // Noncompliant {{Move this "if" to a new line or add the missing "else".}}
//^> ^^

  }

  if ($x) doSomething(); if ($x) { } // Compliant
  if ($x) { } if ($x) { } // Compliant
