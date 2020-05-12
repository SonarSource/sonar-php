<?php

  switch($a) { // Compliant
//^^^^^^> {{Parent "switch" statement}}
    case "foo":
      switch($b) { // Noncompliant {{Refactor this code to eliminate this nested "switch" statement.}}
  //  ^^^^^^
        case "bar":
      }
  }

switch($c) { // Compliant
  case "foo":
}

function f() {
  switch($a) { // Compliant
    case "foo":
      f();
      switch($b) { // Noncompliant
        case "bar":
      }
  }
}
