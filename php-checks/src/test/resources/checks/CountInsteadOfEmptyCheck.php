<?php


if (count($a) > 0) { // Noncompliant
//  ^^^^^^^^^^^^^
  echo $a[0];
}

if (sizeof($a) > 0) { // Noncompliant
//  ^^^^^^^^^^^^^^
  echo $a[0];
}

if (count($z_3) != 0) { // Noncompliant
  echo $z_3[0];
}

if (count($z_9) >= 1) { // Noncompliant
  echo $z_9[0];
}

if (0 != count($z_10)) { // Noncompliant
  echo $z_10[0];
}

if (1 <= count($z_11)) { // Noncompliant
  echo $z_11[0];
}

if (count($z_12) >= 1) { // Noncompliant
  echo $z_12[0];
}

if (count($_POST) > 0) { // Noncompliant
  echo $a;
}

if (count($a_2) >= 0) { // Compliant
  echo $a_2;
}

if (0 < count($a_3)) { // Noncompliant
  echo $a_3[0];
}

if (count($a_4) >= 2) { // Compliant
  echo $a_4[1];
}

$b = getData();
echo $b[0];
if (count($b) > 0) { // Noncompliant
  echo "foo";
}

if (count($c) > 0) { // Compliant - We are not sure that $c is an array
  echo "foo";
}

if (foo($c_1) > 0) { // Compliant
  echo $c_1[0];
}

if (foo($c_2)) {
  echo $c_2[0];
}

function f(array $d) {
  if (count($d) > 0) { // Noncompliant
    echo "foo";
  }
}

function f_2(string $d) {
  if (count($d) > 0) { // Compliant
    echo "foo";
  }
}

function f_3(SomeObject $d) {
  if (count($d) > 0) { // Compliant
    echo "foo";
  }
}

function f_4($d) {
  if (count($d) > 0) { // Compliant
    echo "foo";
  }
}

function union_type(array|Foo $d) {
  if (count($d) > 0) { // Compliant
    echo "foo";
  }
}

if (count($x)) { // Compliant - FN
  echo $x[0];
}

if (count($y) > "abc") {
  echo $y[0];
}

if (count(z) + 1) { // Compliant
  echo $z[0];
}

if (count()) { // Compliant
  echo $foo[0];
}

if (count(getData()) > 0) { // Compliant
  echo "foo";
}

if (count($z_2, COUNT_RECURSIVE) > 0) { // Noncompliant
  echo $z_2[0];
}

if (count($z_4) === "abc") { // Compliant - Doesn't make sense but for test coverage
  echo $z_4[0];
}

if (count($z_5) === $someVar) { // Compliant
  echo $z_5[0];
}

if (count($z_6) === 2) { // Compliant
  echo $z_6[0];
}


if (count($z_8) >= "abc") { // Compliant
  echo $z_8[0];
}

function named_arguments() {
  count(array_or_countable: $_POST) > 0; // Noncompliant
  count(mode: 1, array_or_countable: $_POST) > 0; // Noncompliant
  count(mode: 1, array_or_countable: $unknown_var) > 0;
  sizeof(mode: 1, array_or_countable: $_POST) > 0; // Noncompliant
  sizeof(mode: 1, array_or_countable: $unknown_var) > 0;
}

$example = (!empty($exampleName) && count($exampleName) > 0) ? $exampleName[0] : '';
$example2 = (empty($exampleName2) || count($exampleName2) == 0) ? '' : $exampleName2[0];
if (!empty($exampleName3) && count($exampleName3) > 0) {
  echo $exampleName3[0];
}
if (empty($exampleName4) or count($exampleName4) == 0) {
  echo "empty";
}

if (!empty($differentVar) && count($exampleName5) > 0) { // Noncompliant
  echo $exampleName5[0];
}

// Test nested logical expressions with empty()
if (!empty($nested1) && (count($nested1) > 0 && $nested1[0] == 'test')) {
  echo $nested1[0]; // Compliant - empty() is used
}

// Test with parentheses around empty()
if ((!empty($parens1)) && count($parens1) > 0) {
  echo $parens1[0]; // Compliant - empty() is used with parentheses
}

// Test with alternative operators (and, or) with different positions
if (!empty($altOp1) and count($altOp1) > 0) {
  echo $altOp1[0]; // Compliant - empty() is used with 'and'
}

// Test with 'or' operator
if (empty($orOp1) or count($orOp1) == 0) {
  echo "empty"; // Compliant - empty() is used with 'or'
}

// Test ternary operator with empty() in condition
$ternary1 = !empty($ternVar1) && count($ternVar1) > 0 ? $ternVar1[0] : ''; // Compliant - empty() is used

// Test without empty() - should raise issue
if ($someFlag && count($noEmpty1) > 0) { // Noncompliant
  echo $noEmpty1[0];
}

// Test statement boundary - assignment
$assignment1 = count($assign1) > 0; // Noncompliant
if (!empty($assign1)) {
  echo $assign1[0];
}

// Test statement boundary - return statement
function testReturn(array $ret1) {
  if (count($ret1) > 0) { // Noncompliant
    return true;
  }
  return false;
}

// Test in while statement
while (count($while1) > 0) { // Noncompliant
  echo $while1[0];
  array_shift($while1);
}

// Test in for statement
for ($i = 0; count($for1) > 0; $i++) { // Noncompliant
  echo $for1[0];
  array_shift($for1);
}

// Test in foreach with empty() - should not raise
if (!empty($foreach1)) {
  foreach ($foreach1 as $item) {
    if (count($foreach1) > 0) { // Compliant - crosses foreach statement boundary
      echo $item;
    }
  }
}

// Test complex nested logical with multiple empty() calls
if (!empty($complex1) && !empty($complex2) && count($complex1) > 0) {
  echo $complex1[0]; // Compliant - empty() is used on same variable
}

// Test with triple nested logical expressions
if (($a || !empty($triple1)) && ($b && count($triple1) > 0)) {
  echo $triple1[0]; // Compliant - empty() is used
}

// Test method call boundary - empty() should not cross function boundaries
function testFunctionBoundary(array $boundary1) {
  return count($boundary1) > 0; // Noncompliant
}
if (!empty($boundary1)) {
  echo testFunctionBoundary($boundary1);
}

// Test with sizeof() instead of count()
if (!empty($sizeof1) && sizeof($sizeof1) > 0) {
  echo $sizeof1[0]; // Compliant - empty() is used
}
