<?php

namespace Foo {
  try {}
  catch (Exception $e) { } // Noncompliant
}

try {}
catch (Exception $e) { } // OK - Global namespace context

namespace {
  try {}
  catch (Exception $e) { } // OK - Global namespace context
}
