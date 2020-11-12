<?php

class A extends Exception {}
class B extends Exception {}
class A1 extends A {}
class A11 extends A1 {}
class A2 extends A {}

try {}
catch (A1 $e) {}

try {}
catch (A $e) {}
//     ^> {{A parent exception class is caught here.}}
catch (A1 $e) {} // Noncompliant
//     ^^

try {}
catch (A1 $e) {}
catch (A $e) {}
catch (A11 $e) {} // Noncompliant

try {}
catch (B $e) {}
catch (A1 $e) {}

try {}
catch (A1 $e) {}
catch (A $e) {}

try {}
catch (A $e) {}
//     ^> {{The same exception class is caught here.}}
catch (A $e) {} // Noncompliant
//     ^

try {}
catch (A1 $e) {}
catch (A2 $e) {}

try {}
catch (B | A $e) {}
catch (A1 $e) {} // Noncompliant

try {}
catch (B $e) {}
catch (A | A1 $e) {} // should be handled by S5713

try {}
catch (A $e) {}
catch (B | A1 $e) {}

try {}
catch (A $e) {}
catch (A1 | A2 $e) {} // Noncompliant 2

try {}
catch (A1 | A1 $e) {}

try {}
catch (\Exception $e) {}
catch (\RuntimeException $e) {} // Noncompliant
