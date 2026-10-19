<?php

function finite_dynamic_reads() {
  $first = 3; // Compliant
  $second = 4; // Compliant
  $unused = 9; // Compliant - unresolved dynamic reads can access any local
  $result = [];
  foreach (['first', 'second'] as $name) {
    $result[] = $$name;
  }
  return $result;
}

function literal_dynamic_reads() {
  $first = 3; // Compliant
  $second = 4; // Compliant
  $third = 5; // Compliant
  $unused = 9; // Noncompliant
  return [${'first'}, ${"second"}, ${('third')}];
}

function escaped_literal_name() {
  $value = 1; // Compliant
  return ${"\x76alue"};
}

function unknown_literal_name() {
  $unused = 1; // Noncompliant
  return ${'missing'};
}

function case_sensitive_literal_name() {
  $value = 1; // Noncompliant
  $Value = 2; // Compliant
  return ${'Value'};
}

function compound_dynamic_read($name) {
  $value = 1; // Compliant
  return ${$name};
}

function interpolated_dynamic_read($name) {
  $value = 1; // Compliant
  return "${$name}";
}

function ordinary_interpolation() {
  $value = 1; // Compliant
  $items = [[2]]; // Compliant
  $unused = 2; // Noncompliant
  return "${value} ${items[0][0]}";
}

function ordinary_heredoc_interpolation() {
  $value = 1; // Compliant
  $unused = 2; // Noncompliant
  return <<<TEXT
${value}
TEXT;
}

function constant_dynamic_name() {
  $value = 1; // Compliant
  return ${VARIABLE_NAME};
}

function write_only_dynamic_assignments($name) {
  $unused = 1; // Noncompliant
  $value = 0; // Noncompliant
  $$name = 2;
  ${$name} = 3;
  ${'value'} = 4;
}

function write_only_reference_assignment($name) {
  $unused = 1; // Noncompliant
  $value = 0; // Compliant
  $$name =& $value;
}

function write_only_destructuring($name) {
  $unused = 1; // Noncompliant
  [$$name] = [2];
  list(${$name}) = [3];
}

function write_only_foreach($name, $values) {
  $unused = 1; // Noncompliant
  foreach ($values as $$name) {
  }
  foreach ($values as &${$name}) {
  }
  foreach ($values as $$name => ${$name}) {
  }
}

function compound_dynamic_assignment($name) {
  $value = 1; // Compliant
  $$name += 1;
}

function coalescing_dynamic_assignment($name) {
  $value = 1; // Compliant
  $$name ??= 1;
}

function increment_dynamic_variable($name) {
  $value = 1; // Compliant
  $$name++;
}

function multiple_indirection_write($name) {
  $selector = 'target'; // Compliant - an intermediate variable name is read
  $$$name = 2;
}

function dynamic_selector_write($name) {
  $selector = 'target'; // Compliant
  ${$$name} = 2;
}

function ordinary_dynamic_property($object, $name) {
  $unused = 1; // Noncompliant
  $value = 'property'; // Compliant
  echo $object->$value;
  echo $object->{$name};
  echo $object?->$name;
  $object->$name = 2;
}

function ordinary_dynamic_static_property($name) {
  $unused = 1; // Noncompliant
  echo Example::$$name;
  echo Example::${$name};
  Example::$$name = 2;
}

function dynamic_local_as_property_name($object, $name) {
  $propertyName = 'property'; // Compliant
  return $object->$$name;
}

function dynamic_local_as_static_property_name($name) {
  $propertyName = 'property'; // Compliant
  return Example::$$$name;
}

function closure_scope_isolation() {
  $unused = 1; // Noncompliant
  consume(function ($name) {
    $value = 2; // Compliant
    return $$name;
  });
}

function captured_dynamic_read() {
  $value = 1; // Compliant
  $unused = 2; // Noncompliant
  return function ($name) use ($value) { // Compliant
    return $$name;
  };
}

function reference_capture_dynamic_read() {
  $value = 0; // Compliant
  consume(function () use (&$value) { // Compliant
    $value = 7;
  });
  return function ($name) use ($value) { // Compliant
    return $$name;
  };
}

function literal_captured_dynamic_read() {
  $value = 1; // Compliant
  $unused = 2; // Noncompliant
  return function () use ($value) { // Compliant
    return ${'value'};
  };
}

function nested_function_scope_isolation() {
  $unused = 1; // Noncompliant
  function nested_dynamic_read($name) {
    $value = 2; // Compliant
    return $$name;
  }
}

function outer_dynamic_read($name) {
  $value = 1; // Compliant
  consume(function () {
    $unused = 2; // Noncompliant
  });
  return $$name;
}

class DynamicReadScopes {
  function dynamic_read($name) {
    $value = 1; // Compliant
    return $$name;
  }

  function ordinary_method() {
    $unused = 1; // Noncompliant
  }
}

echo $$globalName;

function unaffected_by_global_dynamic_read() {
  $unused = 1; // Noncompliant
}
