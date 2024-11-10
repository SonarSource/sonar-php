<?php

fizz();                  //Noncompliant {{Remove the usage of this other forbidden function.}}
fizz(1);                 //Noncompliant

buzz();                  //Noncompliant
buzz(1);                 //Noncompliant

class Obj
{
    public function fizz()
    {
        return "fizz";
    }

    public function buzz()
    {
        return "buzz";
    }
}

$myObj = new Obj();

$myObj->fizz();            // OK
$myObj->buzz();            // OK
