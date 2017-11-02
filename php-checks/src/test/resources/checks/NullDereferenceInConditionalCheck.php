<?php

class A {
  public $field = 42;
}

function x($obj){
  if($obj == null && $obj->field == 0){}         // Noncompliant {{Either reverse the equality operator in the "$obj" null test, or reverse the logical operator that follows it.}}
//   ^^^^
  if($obj == null and $obj->field == 0){}         // Noncompliant
  if(is_null($obj) && $obj->field == 0){}        // Noncompliant
//           ^^^^
  if(!is_null($obj) || $obj->field == 0){}       // Noncompliant
  if($obj == null && $obj::$staticField == 0){}  // Noncompliant
  if($obj != null || $obj->field > 0){}          // Noncompliant
  if($obj != null || $obj->method()){}          // Noncompliant
  if($obj != null or $obj->field > 0){}          // Noncompliant
  if($obj == null || $obj->field == 0){}
  if($obj != null && $obj->field > 0){}
  if($obj == null && other.length == 0){}
  if($a == null && ($b != null || $b->field>0)){}   // Noncompliant
  if(($obj) == null && $obj->field == 0){}       // Noncompliant
  if(($obj == null) && $obj->field == 0){}       // Noncompliant
  if($obj == null && ($obj->field == 0)){}       // Noncompliant
  if($obj == null && y($obj->field)){}           // Noncompliant
  if($obj == null && ($obj = a) == null){}
  if($obj == null && $obj == a){}
  if($obj === null && $obj->field == 0){}        // Noncompliant
  if($obj !== null && $obj->field == 0){}
  if($obj !== null || $obj->field > 0){}         // Noncompliant
  if($obj === null || $obj->field > 0){}
  if(null == $obj && $obj->field == 0){}         // Noncompliant
  if ($obj->field == null && ($obj->field)->foo()) {}   // Noncompliant
  if (!is_null($obj->field) || ($obj->field)->foo()) {}   // Noncompliant
}
