<?php 

foo(($a+1), $a+1); // Noncompliant {{Verify that this is the intended value; it is the same as the 1st argument.}}
//   ^^^^>  ^^^^

foo($a+1,
    $a+1,  // Noncompliant {{Verify that this is the intended value; it is the same as the 1st argument.}}
    $a+1); // Noncompliant {{Verify that this is the intended value; it is the same as the 1st argument.}}

foo(null, $a+1, $a+1); // Noncompliant {{Verify that this is the intended value; it is the same as the 2nd argument.}}
foo(null, null, $a+1, $a+1); // Noncompliant {{Verify that this is the intended value; it is the same as the 3rd argument.}}
foo(null, null, null, $a+1, $a+1); // Noncompliant {{Verify that this is the intended value; it is the same as the 4th argument.}}

foo(bar($a), bar($a)); // Noncompliant
foo($foo->{$otherObj->var}, $foo->{$otherObj->var}); // Noncompliant
//  ^^^^^^^^^^^^^^^^^^^^^^> ^^^^^^^^^^^^^^^^^^^^^^

foo($a);
foo($a, $a);
foo(null, null);
foo(false, false);
foo([], []);
foo([2], [2]); // Noncompliant
foo(true, true);
foo(0, 0);
foo("hello", "hello");
foo($this->$a, $this->$a);
foo($this->a, $this->a);
foo(array(), array());
foo(array('foo' => 'bar'), array('foo' => 'bar')); // Noncompliant
foo(new A(), new A());
foo($a+1, $a+2);
