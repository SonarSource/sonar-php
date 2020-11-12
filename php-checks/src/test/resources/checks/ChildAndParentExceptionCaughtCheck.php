<?php

class CustomException extends Exception {}
class OtherException extends Exception {}

// duplicated exception
try {}
catch (CustomException | CustomException $e) {} // Noncompliant {{Remove this duplicate Exception class.}}
//     ^^^^^^^^^^^^^^^   ^^^^^^^^^^^^^^^< {{Duplicate.}}

try {}
catch (OtherException | CustomException | OtherException $e) {} // Noncompliant
//     ^^^^^^^^^^^^^^                     ^^^^^^^^^^^^^^<

try {}
catch (CustomException | FooException | FooException $e) {} // Noncompliant
//                       ^^^^^^^^^^^^   ^^^^^^^^^^^^<

// derived exception
class CustomException1 extends Exception {}
try {}
catch (Exception | CustomException1 $e) {} // Noncompliant {{Remove this useless Exception class; it derives from class exception which is already caught.}}
//     ^^^^^^^^^>  ^^^^^^^^^^^^^^^^

class CustomException2 extends Exception {}
try {}
catch (CustomException2 | Exception $e) {} // Noncompliant {{Remove this useless Exception class; it derives from class exception which is already caught.}}
//     ^^^^^^^^^^^^^^^^   ^^^^^^^^^< {{Parent class.}}

try {}
catch (CustomSubException | CustomException | Exception $e) {} // Noncompliant 2

class CustomSubException extends CustomException {}

// derived exception with another known exception
try {}
catch (OtherException | CustomException | CustomSubException $e) {} // Noncompliant
//                      ^^^^^^^^^^^^^^^>  ^^^^^^^^^^^^^^^^^^

// derived exception with another unknown exception
try {}
catch (FooException | CustomException | CustomSubException $e) {} // Noncompliant
//                                      ^^^^^^^^^^^^^^^^^^

try {}
catch (CustomException | OtherException $e) {} // Compliant

try {}
catch (Exception $e) {} // Compliant
