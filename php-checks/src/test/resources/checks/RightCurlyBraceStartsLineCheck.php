<?php

function f (){
  dorSomthing();}              // NOK

if (true){
  doSomething();}              // NOK

$a = function () {
  doSomething;};               // NOK

{
  echo $a;}                    // NOK

$var->
{'user_' . $id};               // OK

if (true) { doSomething(); }   // OK

$var->{'user_' . $id};         // OK


