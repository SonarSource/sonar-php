<?php

namespace DynamicLocalReads;

echo $$name;
echo ${'value'};
include 'included.php';

function finite_dynamic_reads() {
  $first = 3; // Compliant
  $second = 4; // Compliant
  $unused = 9; // Compliant - FN: the dynamic reads only access $first and $second.
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

function case_sensitive_literal_name() {
  $value = 1; // Noncompliant
  $Value = 2; // Compliant
  return ${'Value'};
}

function compound_dynamic_read($name) { $value = 1; return ${$name}; } // Compliant
function interpolated_dynamic_read($name) { $value = 1; return "${$name}"; } // Compliant

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

const VARIABLE_NAME = 'value';

function constant_dynamic_name() { $value = 1; return ${VARIABLE_NAME}; } // Compliant

function write_only_dynamic_operations($name, $values) {
  $unused = 1; // Noncompliant
  $value = 0; // Noncompliant
  $source = 0; // Compliant
  $$name = 2;
  ${$name} = 3;
  ${'value'} = 4;
  $$name =& $source;
  [$$name] = [2];
  list(${$name}) = [3];
  foreach ($values as $$name) {
  }
  foreach ($values as &${$name}) {
  }
  foreach ($values as $$name => ${$name}) {
  }
}

function compound_dynamic_assignment($name) { $value = 1; $$name += 1; } // Compliant
function coalescing_dynamic_assignment($name) { $value = 1; $$name ??= 1; } // Compliant
function increment_dynamic_variable($name) { $value = 1; $$name++; } // Compliant

function multiple_indirection_write($name) {
  $selector = 'target'; // Compliant - an intermediate variable name is read
  $$$name = 2;
}

function dynamic_selector_write($name) {
  $selector = 'target'; // Compliant
  ${$$name} = 2;
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

namespace IndirectLocalReads;

function included_code_can_read_locals() { $value = 1; include 'included.php'; } // Compliant
function code_included_once_can_read_locals() { $value = 1; include_once('included.php'); } // Compliant
function required_code_can_read_locals() { $value = 1; require 'included.php'; } // Compliant
function code_required_once_can_read_locals() { $value = 1; require_once('included.php'); } // Compliant
function evaluated_code_can_read_locals($code) { $value = 1; eval($code); } // Compliant
function defined_vars_reads_locals() { $value = 1; return get_defined_vars(); } // Compliant
function qualified_mixed_case_indirect_access() { $value = 1; return \GeT_DeFiNeD_VaRs(); } // Compliant

function extract_can_observe_existing_locals($values) {
  $value = 1; // Compliant - EXTR_SKIP checks whether the name already exists.
  extract($values, EXTR_SKIP);
}

function calls_without_indirect_local_access($object) {
  $unused = 1; // Noncompliant
  func_get_arg(0);
  func_get_args();
  $object->get_defined_vars();
  Example::extract([]);
  Other\get_defined_vars();
  new get_defined_vars();
  get_defined_vars(...);
}

function outer_indirect_access_does_not_suppress_nested_locals() {
  $value = 1; // Compliant
  include 'included.php';
  consume(function () {
    $unused = 2; // Noncompliant
  });
}

function nested_indirect_access_does_not_suppress_outer_locals() {
  $unused = 1; // Noncompliant
  consume(function () {
    $value = 2; // Compliant
    require 'included.php';
  });
  function nested_indirect_access() {
    $value = 3; // Compliant
    eval('echo $value;');
  }
  consume(fn() => get_defined_vars());
}

function reference_capture_indirect_read() {
  $value = 0; // Compliant
  $unused = 2; // Noncompliant
  consume(function () use (&$value) { // Compliant
    $value = 7;
  });
  return function () use ($value) { // Compliant
    include 'included.php';
  };
}

function anonymous_class_indirect_access() {
  $unused = 1; // Noncompliant
  return new class {
    function method() {
      $value = 2; // Compliant
      return get_defined_vars();
    }
  };
}

function anonymous_class_argument_indirect_access() {
  $value = 1; // Compliant
  return new class(get_defined_vars()) {
    function __construct($vars) {
      $unused = 2; // Noncompliant
    }
  };
}

namespace MemberNames;

/*
 * With $name = 'member' and $member = 'foo':
 *   Example::$$name       reads Example::$member; $object->$$name reads $member, then $object->foo.
 *   Example::$$name()     and $object->$$name() both read $member to call the method foo().
 *   Example::$$name(...)  and $object->$$name(...) likewise read $member to create a callable.
 *   new Example::$$name() reads the class from Example::$member;
 *   new $object->$$name() reads $member, then the class from $object->foo.
 */
function static_property_names($object, $name) {
  $member = 'foo'; // Noncompliant
  $value = 'property'; // Compliant
  echo $object->$value;
  echo $object->{$name};
  echo $object?->$name;
  $object->$name = 2;
  echo Example::$$name;
  echo Example::${$name};
  Example::$$name = 2;
  return new Example::$$name();
}

function dynamic_instance_members($object, $name) {
  $member = 'foo'; // Compliant
  echo $object->$$name;
  $object->$$name();
  return new $object->$$name();
}

function dynamic_static_method($name) {
  $member = 'foo'; // Compliant
  return Example::$$name();
}

function dynamic_local_as_static_property_name($name) { $propertyName = 'property'; return Example::$$$name; } // Compliant
