<?php

function f () {
  doSomthing();}               // NOK {{Move this closing curly brace to the next line.}}

if (true){
  doSomething();}              // NOK {{Move this closing curly brace to the next line.}}

$a = function () {
  doSomething;};               // NOK {{Move this closing curly brace to the next line.}}

{
  echo $a;}                    // NOK {{Move this closing curly brace to the next line.}}

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
    Foo as bar; }                    // NOK {{Move this closing curly brace to the next line.}}

  use SomeTrait; }                   // NOK {{Move this closing curly brace to the next line.}}

switch (a) {
  case 1 :
      break; }                        // NOK {{Move this closing curly brace to the next line.}}

switch (a) {
  ; }                        // NOK {{Move this closing curly brace to the next line.}}
