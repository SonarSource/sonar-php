<?php

namespace Example\EnumCases;

use Enum;

class MixedValues extends Enum {
  public const PUBLIC_VALUE = 'public';
  private const STRING_VALUE = 'string'; // Compliant
  private const NUMBER = 2; // Compliant
  private const NEGATIVE_NUMBER = -2; // Compliant
  private const FLAG = true; // Compliant
  private const EMPTY_VALUE = null; // Compliant
  private const NON_SCALAR = ['x']; // Compliant

  public function getPublicValue() {
    return self::PUBLIC_VALUE;
  }
}

namespace Example\OtherEnumCases;

use Vendor\Enum as EnumBase;

class MultipleConstants extends \Vendor\Enum {
  private const FIRST = 'first'; // Compliant
  private const SECOND = 'second'; // Compliant
  private const THIRD = 'third'; // Compliant
  private const FOURTH = 'fourth'; // Compliant
  private const FIFTH = 'fifth'; // Compliant
  private const SIXTH = 'sixth'; // Compliant
  private const SEVENTH = 'seventh'; // Compliant
}

class AliasedEnum extends EnumBase {
  private const VALUE = 'value'; // Compliant
}

class WithInstanceProperty extends \Vendor\Enum {
  private $property; // Noncompliant
  private const VALUE = 'value'; // Noncompliant
}

class WithStaticProperty extends \Vendor\Enum {
  private static $property; // Noncompliant
  private const VALUE = 'value'; // Noncompliant
}

class WithPromotedProperty extends \Vendor\Enum {
  private const VALUE = 'value'; // Noncompliant

  public function __construct(private $property) { // Noncompliant
  }
}

class AnotherBase extends \Vendor\NotEnum {
  private const VALUE = 'value'; // Noncompliant
}
