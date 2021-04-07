<?php

try {}
catch (Exception $e) { } // OK - Exception is in global namespace

//-------------------------------------------------------------------------
namespace Foo;

try {}
catch (Exception $e) { } // Noncompliant {{Create class "Exception" in namespace or check correct import of class}}

//-------------------------------------------------------------------------
namespace Bar;

class Exception {}

try {}
catch (Exception $e) {} // OK - Class exists in namespace

//-------------------------------------------------------------------------
namespace Foo\Bar;

try {}
catch (\Exception $e) {} // OK - Exception declared with global namespace

//-------------------------------------------------------------------------
namespace Bar\Foo;

use Exception;

try {}
catch (Exception $e) {} // OK - Exception is used from global namespace

//-------------------------------------------------------------------------
