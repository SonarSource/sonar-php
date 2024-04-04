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

class ConstructorPropertyPromotionAndReadOnlyConstructorArguments {     // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
  public $f1;
  public $f2;
  public function __construct(
    protected readonly string $f3,
    private readonly $f4,
    readonly private string $f5,
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

class Constants {   // Compliant
  public const A1 = 'a1';
  public const A2 = 'a2';
  public const A3 = 'a3';
  public const A4 = 'a4';
  public const A5 = 'a5';
}

class ConstantsAndPublicField {   // Compliant
  public const A1 = 'a1';
  public const A2 = 'a2';
  public const A3 = 'a3';
  public const A4 = 'a4';
  public $f1;
}

class ConstantsAndProtectedField {   // Compliant
  public const A1 = 'a1';
  public const A2 = 'a2';
  public const A3 = 'a3';
  public const A4 = 'a4';
  protected $f1;
}

class ConstantsAndPrivateField {   // Compliant
  public const A1 = 'a1';
  public const A2 = 'a2';
  public const A3 = 'a3';
  public const A4 = 'a4';
  private $f1;
}

class ConstantsAndConstructorPropertyPromotionPublic {     // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
  public const A1 = 'a1';
  public const A2 = 'a2';
  public const A3 = 'a3';
  public const A4 = 'a4';
  public const A5 = 'a5';
  public function __construct(
    public string $f1,
    public string $f2,
    public string $f3,
    public string $f4,
    public string $f5,
  ) {}
};

class ConstantsAndConstructorPropertyPromotionPublic {     // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
  PUBLIC const A1 = 'a1';
  PUBLIC const A2 = 'a2';
  PUBLIC const A3 = 'a3';
  PUBLIC const A4 = 'a4';
  PUBLIC const A5 = 'a5';
  public function __construct(
    PUBLIC string $f1,
    PUBLIC string $f2,
    PUBLIC string $f3,
    PUBLIC string $f4,
    PUBLIC string $f5,
  ) {}
};

class ConstantsAndConstructorPropertyPromotionPublic {     // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
  public const A1 = 'a1';
  public const A2 = 'a2';
  public const A3 = 'a3';
  public const A4 = 'a4';
  public function __construct(
    public string $f1,
    public string $f2,
    public string $f3,
    public string $f4,
    public string $f5,
  ) {}
};

class ConstantsAndConstructorPropertyPromotionAndReadOnlyConstructorArguments {     // Noncompliant {{Refactor this class so it has no more than 4 fields, rather than the 5 it currently has.}}
  public const A1 = 'a1';
  public const A2 = 'a2';
  public const A3 = 'a3';
  public const A4 = 'a4';
  public const A5 = 'a5';
  public const A6 = 'a6';
  public $f1;
  public $f2;
  public function __construct(
    protected readonly string $f3,
    private readonly $f4,
    readonly private string $f5,
    $notPropertyPromotion1,
    $notPropertyPromotion2
  ) {}
};
