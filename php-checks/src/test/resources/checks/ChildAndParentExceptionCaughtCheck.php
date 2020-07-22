<?php

class CustomException extends Exception {}
class CustomSubException extends CustomException {}
class OtherException extends Exception {}

// duplicated exception
try {}
catch (CustomException | CustomException $e) {} // Noncompliant {{Remove this duplicate Exception class.}}
//     ^^^^^^^^^^^^^^^   ^^^^^^^^^^^^^^^<

try {}
catch (OtherException | CustomException | OtherException $e) {} // Noncompliant
//     ^^^^^^^^^^^^^^                     ^^^^^^^^^^^^^^<

try {}
catch (CustomException | FooException | FooException $e) {} // Noncompliant
//                       ^^^^^^^^^^^^   ^^^^^^^^^^^^<

// derived exception
try {}
catch (Exception | CustomException $e) {} // Noncompliant {{Remove this useless Exception class; it derives from another which is also caught.}}
//     ^^^^^^^^^>  ^^^^^^^^^^^^^^^

try {}
catch (CustomException | Exception $e) {} // Noncompliant
//     ^^^^^^^^^^^^^^^   ^^^^^^^^^<

try {}
catch (CustomSubException | CustomException | Exception $e) {} // Noncompliant 2

// derived exception with another known exception
try {}
catch (OtherException | CustomException | CustomSubException $e) {} // Noncompliant
//                      ^^^^^^^^^^^^^^^>  ^^^^^^^^^^^^^^^^^^

// derived exception with another unknown exception
try {}
catch (FooException | CustomException | CustomSubException $e) {} // Noncompliant
//                    ^^^^^^^^^^^^^^^>  ^^^^^^^^^^^^^^^^^^

try {}
catch (CustomException | OtherException $e) {} // Compliant

try {}
catch (Exception $e) {} // Compliant
