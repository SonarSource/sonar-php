<?php

switch($a) { // Compliant
  case "foo":
    switch($b) { // Noncompliant
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
