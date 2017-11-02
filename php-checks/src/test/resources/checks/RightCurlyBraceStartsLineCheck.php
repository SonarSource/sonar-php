<?php

function f () {
  doSomthing();}               // Noncompliant {{Move this closing curly brace to the next line.}}
//             ^

if (true){
  doSomething();}              // Noncompliant {{Move this closing curly brace to the next line.}}

$a = function () {
  doSomething;};               // Noncompliant {{Move this closing curly brace to the next line.}}

{
  echo $a;}                    // Noncompliant {{Move this closing curly brace to the next line.}}

function f() {
  doSomething();
}                              // OK

$var->
{'user_' . $id};               // OK

if (true) { doSomething(); }   // OK

$var->{'user_' . $id};         // OK

${$field} = '';                // OK

class A {
  use SomeTrait, OtherTrait{         // OK
  }

  use SomeTrait, OtherTrait {
    Foo as bar; }                    // Noncompliant {{Move this closing curly brace to the next line.}}

  use SomeTrait; }                   // Noncompliant {{Move this closing curly brace to the next line.}}

switch (a) {
  case 1 :
      break; }                        // Noncompliant {{Move this closing curly brace to the next line.}}

switch (a) {
  ; }                        // Noncompliant {{Move this closing curly brace to the next line.}}

$x = new class {
  use SomeTrait, OtherTrait{         // OK
  }

  use SomeTrait, OtherTrait {
    Foo as bar; }                    // Noncompliant {{Move this closing curly brace to the next line.}}

  use SomeTrait; };                   // Noncompliant {{Move this closing curly brace to the next line.}}
