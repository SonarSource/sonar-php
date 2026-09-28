<?php

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
