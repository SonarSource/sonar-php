<?php

  class A {        // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
//^^^^^
  public $f1;
  public $f2;
  public $f3;
  protected $f4;
  private $f5;
}


class B {
  public $f1;
  public $f2;
}

$x = new class {        // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
//       ^^^^^
  public $f1;
  public $f2;
  public $f3;
  protected $f4;
  private $f5;
};


$x = new class {
  public $f1;
  public $f2;
};

class ConstructorPropertyPromotionPublic {     // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
  public function __construct(
    public string $f1,
    public string $f2,
    public string $f3,
    public string $f4,
    public string $f5,
  ) {}
};

class ConstructorPropertyPromotionPublic {     // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
  public function __construct(
    PUBLIC string $f1,
    PUBLIC string $f2,
    PUBLIC string $f3,
    PUBLIC string $f4,
    PUBLIC string $f5,
  ) {}
};

class ConstructorPropertyPromotionProtected {     // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
  public function __construct(
    protected string $f1,
    protected string $f2,
    protected string $f3,
    protected string $f4,
    protected string $f5,
  ) {}
};

class ConstructorPropertyPromotionPrivate {     // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
  public function __construct(
    private string $f1,
    private string $f2,
    private string $f3,
    private string $f4,
    private string $f5,
  ) {}
};

class ConstructorPropertyPromotionMixed {     // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
  public function __construct(
    public string $f1,
    protected string $f2,
    private string $f3,
    private string $f4,
    private string $f5,
  ) {}
};

class FieldsAndConstructorPropertyPromotion {     // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
  public $f1;
  public $f2;
  public function __construct(
    public string $f3,
    public string $f4,
    public string $f5,
  ) {}
};

class ConstructorPropertyPromotionAndRegularConstructorArguments {     // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
  public $f1;
  public $f2;
  public function __construct(
    protected string $f3,
    protected string $f4,
    protected string $f5,
    $notPropertyPromotion1,
    $notPropertyPromotion2
  ) {}
};

class ConstructorPropertyPromotionAndRegularConstructorArgumentsCompliant {     // Compliant
  public $f1;
  public $f2;
  public function __construct(
    private string $f3,
    private string $f4,
    $notPropertyPromotion1,
    $notPropertyPromotion2
  ) {}
};
