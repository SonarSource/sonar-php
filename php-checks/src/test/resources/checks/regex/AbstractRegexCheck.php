<?php

namespace checks\regex;

class AbstractRegexCheck
{
  function noncompliant($input)
  {
    preg_match("/foo/", $input); // Noncompliant
  }
}
