<?php

function f () {
  doSomthing();}               // NOK

if (true){
  doSomething();}              // NOK

$a = function () {
  doSomething;};               // NOK

{
  echo $a;}                    // NOK

function f() {
  doSomthing();
}                              // OK

$var->
{'user_' . $id};               // OK

if (true) { doSomething(); }   // OK

$var->{'user_' . $id};         // OK

${$field} = '';                // OK
