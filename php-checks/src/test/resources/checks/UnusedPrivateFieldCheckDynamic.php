<?php

namespace Checks;

use Checks\AliasedStaticProperty as ClassAlias;

class DynamicProperty {
  private $indirectlyUsed; // Compliant
  private static $unusedStatic; // Noncompliant
  private const UNUSED_CONSTANT = 1; // Noncompliant

  public function access($name) {
    echo $this->$name;
    echo $this->{$name};
  }
}

class VariableVariableProperty {
  private $indirectlyUsed; // Compliant

  public function access($name) {
    echo $this->$$name;
  }
}

class CompoundVariableProperty {
  private $indirectlyUsed; // Compliant

  public function access($name) {
    echo $this->${$name};
  }
}

class DynamicNewOperand {
  private $className; // Compliant

  public function create($name) {
    new $this->{$name}();
  }
}

class DynamicMethodOnly {
  private $unused; // Noncompliant

  public function call($name) {
    $this->$name();
    $this->{$name}();
  }
}

class DynamicMethodReferenceOnly {
  private $unused; // Noncompliant

  public function reference($name) {
    return $this->{$name}(...);
  }
}

class OtherReceiverOnly {
  private $unused; // Noncompliant

  public function read($other, $name) {
    echo $other->$name;
  }
}

class ExactStringOnly {
  private $known; // Compliant
  private $unused; // Noncompliant

  public function read() {
    echo $this->{'known'};
  }
}

class DynamicAccessWithFinitePropertyNames {
  private const ALLOWED_PROPERTIES = ['selected'];

  private $selected; // Compliant, accessed through the allowed property name
  private $unused; // FN: not in ALLOWED_PROPERTIES, but broad dynamic access suppresses the issue

  public function get($name) {
    if (in_array($name, self::ALLOWED_PROPERTIES, true)) {
      return $this->{$name};
    }
    return null;
  }
}

class EscapedStringName {
  private $possiblyRead; // Compliant
  private static $unusedStatic; // Noncompliant
  private const UNUSED_CONSTANT = 1; // Noncompliant

  public function read() {
    // The literal has a fixed name, but the check does not decode its escapes
    // and conservatively suppresses instance fields.
    echo $this->{'\\name'};
  }
}

class DynamicAccessOnlyInNestedNamedClass {
  private $outerUnused; // Noncompliant

  public function createInner() {
    class NestedClassWithDynamicAccess {
      private $innerIndirectlyUsed; // Compliant

      public function read($name) {
        echo $this->$name;
      }
    }
  }
}

class DynamicAccessOnlyInAnonymousClass {
  private $outerUnused; // Noncompliant

  public function createInner() {
    return new class {
      public function read($name) {
        echo $this->$name;
      }
    };
  }
}

class DynamicAccessInAnonymousClassConstructorArgument {
  private $indirectlyUsed; // Compliant

  public function create($name) {
    return new class($this->$name) {
      public function __construct($value) {
      }
    };
  }
}

class VariableVariableStaticProperty {
  private static $indirectlyUsed; // Compliant
  private $unusedInstance; // Noncompliant
  private const UNUSED_CONSTANT = 1; // Noncompliant

  public static function read($name) {
    return self::$$name;
  }
}

class CompoundVariableStaticProperty {
  private static $indirectlyUsed; // Compliant

  public static function read($name) {
    return self::${$name};
  }
}

class LateBoundStaticProperty {
  private static $indirectlyUsed; // Compliant

  public static function read($name) {
    return static::${$name};
  }
}

class StaticPropertyThroughThis {
  private static $indirectlyUsed; // Compliant
  private $unusedInstance; // Noncompliant

  public function read($name) {
    return $this::$$name;
  }
}

class NamedStaticProperty {
  private static $indirectlyUsed; // Compliant

  public static function read($name) {
    return NamedStaticProperty::$$name;
  }
}

class FullyQualifiedStaticProperty {
  private static $indirectlyUsed; // Compliant

  public static function read($name) {
    return \Checks\FullyQualifiedStaticProperty::${$name};
  }
}

class AliasedStaticProperty {
  private static $indirectlyUsed; // Compliant

  public static function read($name) {
    return ClassAlias::$$name;
  }
}

class FixedStaticPropertyName {
  private static $known; // Compliant
  private static $unused; // Noncompliant

  public static function read() {
    return self::$known;
  }
}

class ExactStaticPropertyName {
  private static $known; // Compliant
  private static $unused; // Noncompliant
  private $unusedInstance; // Noncompliant

  public static function read() {
    return self::${'known'};
  }
}

class StaticSyntaxDoesNotAccessInstanceProperty {
  private $known; // Noncompliant

  public static function read() {
    return self::${'known'};
  }
}

class InstanceSyntaxDoesNotAccessStaticProperty {
  private static $known; // Noncompliant

  public function read() {
    return $this->{'known'};
  }
}

class DynamicStaticMethodOnly {
  private static $unused; // Noncompliant
  private const UNUSED_CONSTANT = 1; // Noncompliant

  public static function call($name) {
    self::$name();
    self::$$name();
    self::${$name}();
    self::{$name}();
  }
}

class DynamicStaticMethodReferenceOnly {
  private static $unused; // Noncompliant
  private const UNUSED_CONSTANT = 1; // Noncompliant

  public static function reference($name) {
    // PHP 8.1+: (...) creates a Closure for the dynamically named method without calling it.
    return self::${$name}(...);
  }

  public static function computedReference($name) {
    return self::{$name}(...);
  }
}

class DynamicConstantOnly {
  private const INDIRECTLY_USED = 1; // Compliant
  private static $unused; // Noncompliant
  private $unusedInstance; // Noncompliant

  public static function read($name) {
    return self::{$name};
  }
}

class ExactConstantName {
  private const KNOWN = 1; // Compliant
  // Constant names are case-sensitive.
  private const known = 2; // Noncompliant
  private static $KNOWN; // Noncompliant
  private $unusedInstance; // Noncompliant

  public static function read() {
    return self::{'KNOWN'};
  }
}

// The explicit class name refers to a different namespace, parent targets
// the base class, and $other's runtime class is unresolved.
// None establishes a use of this class's private members.
class OtherStaticReceivers extends \Other\Base {
  private static $unused; // Noncompliant
  private const UNUSED_CONSTANT = 1; // Noncompliant

  public function read($other, $name) {
    echo \Other\OtherStaticReceivers::$$name;
    echo parent::${$name};
    echo $other::$$name;
    echo \Other\OtherStaticReceivers::{$name};
    echo parent::{$name};
    echo $other::{$name};
  }
}

class DynamicStaticNewOperand {
  private static $className; // Compliant
  private $unusedInstance; // Noncompliant

  public static function create($name) {
    return new self::$$name();
  }
}
