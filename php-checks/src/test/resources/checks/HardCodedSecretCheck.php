<?php

// Global Constants
const PASSED = "abcdefghijklmnopqrs";   // Compliant nothing to do with secrets

// Noncompliant@+1 {{'SECRET' detected in this expression, review this potentially hard-coded secret.}}
const MY_SECRET = "abcdefghijklmnopqrs";
//                ^^^^^^^^^^^^^^^^^^^^^

// Noncompliant@+1 {{'api_key' detected in this expression, review this potentially hard-coded secret.}}
const api_key = 'abcdefghijklmnopqrs';

// Noncompliant@+1
const ABC = "abcdefghijklmnopqrs", AUTH = "abcdefghijklmnopqrs", XYZ = "abcdefghijklmnopqrs";
//                                        ^^^^^^^^^^^^^^^^^^^^^

// Noncompliant@+2
const TOKEN_HEREDOC = <<<END
abcdefghijklmnopqrs
END;

// FN it is literal tree and contains new lines
const TOKEN_NOWDOC = <<<'EOD'
abcdefghijklmnopqrs
EOD;

define("AUTH", "abcdefghijklmnopqrs");                    // Noncompliant

define("credential", "abcdefghijklmnopqrs");              // Noncompliant

define("namespace\level\Token", "abcdefghijklmnopqrs");   // Noncompliant


// Variables declarations (as AssignmentExpression)
$var_ok = "abcdefghijklmnopqrs"; // Compliant

$oauth = "abcdefghijklmnopqrs";  // Noncompliant

// Variables in function
function do_something(): void
{
  $a = "abcdefghijklmnopqrs";         // Compliant
  $apikey = "abcdefghijklmnopqrs";   // Noncompliant
}

// Class Constants (as Variables in AST)
class ContainsConstrants
{
  // Noncompliant@+1
  const Token = "abcdefghijklmnopqrs";
//              ^^^^^^^^^^^^^^^^^^^^^

  const API_KEY = "abcdefghijklmnopqrs";  // Noncompliant
  const AUTH = "abcdefghijklmnopqrs";               // Noncompliant

  const OK_CONSTANT = "abcdefghijklmnopqrs";        // Compliant

  // Assignments
  function doSomething()
  {
    $someSecret = "";
    $someSecret = "abcdefghijklmnopqrs";          // Noncompliant

  }
}

// Class Properties
class ContainsProperties
{
  public $passed = 'abcdefghijklmnopqrs';
  public $apikey = 'abcdefghijklmnopqrs';                   // Noncompliant
  public string $api_key = "abcdefghijklmnopqrs";           // Noncompliant

  protected $credential = "abcdefghijklmnopqrs";            // Noncompliant
  private $private_credential = "abcdefghijklmnopqrs";      // Noncompliant
  static $static_credential = "abcdefghijklmnopqrs";        // Noncompliant

  public function __construct(
    public readonly string $auth = "abcdefghijklmnopqrs",             // Noncompliant
    protected              $protected_auth = "abcdefghijklmnopqrs",   // Noncompliant
    private                $private_auth = "abcdefghijklmnopqrs",     // Noncompliant
                           $oauth = "abcdefghijklmnopqrs",            // Noncompliant
                           $empty_auth
  )
  {
  }

}

// Object
$object = (object)[
  'propertyOne' => 'abcdefghijklmnopqrs',
  'secret' => 'abcdefghijklmnopqrs',        // Noncompliant
  "token" => "abcdefghijklmnopqrs"          // Noncompliant
];

// Array
$array = array(
  'passed' => 'abcdefghijklmnopqrs',
  'auth' => 'abcdefghijklmnopqrs',          // Noncompliant
  "credential" => "abcdefghijklmnopqrs"     // Noncompliant
);

function compareStrings($auth)
{
  if ($auth == null)
  {
    return;
  }
  if ($auth == "")
  {
    return;
  }
  if ($auth == "X")
  {
    return;
  }
  if ($auth == "abcdefghijklmnopqrs")                     // Noncompliant
  {
    echo $auth;
  }
  if ("abcdefghijklmnopqrs" == $auth)                     // Noncompliant
  {
    echo $auth;
  }
  if ($auth === "abcdefghijklmnopqrs")                     // Noncompliant
  {
    echo $auth;
  }
  if ("abcdefghijklmnopqrs" === $auth)                     // Noncompliant
  {
    echo $auth;
  }
  if ($auth <=> "abcdefghijklmnopqrs")                     // Noncompliant
  {
    echo $auth;
  }
  if ("abcdefghijklmnopqrs" <=> $auth)                     // Noncompliant
  {
    echo $auth;
  }
  if (PASSED == $auth)                                    // FN unable to resolve the value
  {
    echo $auth;
  }
  if ($auth == PASSED)                                     // FN unable to resolve the value
  {
    echo $auth;
  }

  // case sensitive
  if (strcmp($auth, "abcdefghijklmnopqrs"))         // Noncompliant
  {
    echo $auth;
  }
  if (strcmp("abcdefghijklmnopqrs", $auth))         // Noncompliant
  {
    echo $auth;
  }
  if (strcmp($auth, PASSED))                         // FN unable to resolve the value
  {
    echo $auth;
  }
  if (strcmp(PASSED, $auth))                         // FN unable to resolve the value
  {
    echo $auth;
  }
  if (strcmp(null, $auth))
  {
    echo $auth;
  }
  if (strcmp("", $auth))
  {
    echo $auth;
  }
  if (strcmp("X", $auth))
  {
    echo $auth;
  }

  // case insensitive
  if (strcasecmp($auth, "abcdefghijklmnopqrs"))    // Noncompliant
  {
    echo $auth;
  }
  if (strcasecmp("abcdefghijklmnopqrs", $auth))    // Noncompliant
  {
    echo $auth;
  }
  if (strcasecmp($auth, PASSED))                   // FN unable to resolve the value
  {
    echo $auth;
  }
  if (strcasecmp(PASSED, $auth))                   // FN unable to resolve the value
  {
    echo $auth;
  }
  if (strcasecmp(null, $auth))
  {
    echo $auth;
  }
  if (strcasecmp("", $auth))
  {
    echo $auth;
  }
  if (strcasecmp("X", $auth))
  {
    echo $auth;
  }
}

function some2ArgsFunction($arg1, $arg2)
{
  // do nothing
}

// When a function call has two arguments potentially containing String, we report an issue the same way we would with a variable declaration
function callSome2ArgsFunction()
{
  some2ArgsFunction("secret", "abcdefghijklmnopqrs");        // Noncompliant
  some2ArgsFunction("abcdefghijklmnopqrs", "secret");        // Noncompliant
  some2ArgsFunction('abcdefghijklmnopqrs', 'secret');        // Noncompliant
  some2ArgsFunction("secret", PASSED);                       // FN unable to resolve the value
  some2ArgsFunction(PASSED, "secret");                       // FN unable to resolve the value
  some2ArgsFunction("secret", "X");
  some2ArgsFunction("secret", "");
  some2ArgsFunction("secret", null);
  some2ArgsFunction("secret", 42);
  some2ArgsFunction("secret", 'auth');
  some2ArgsFunction("X", "secret");
  some2ArgsFunction("", "secret");
  some2ArgsFunction(null, "secret");
  some2ArgsFunction(42, "secret");
  some2ArgsFunction('auth', "secret");
}

function callSome2ArgsMethod()
{
  $box = new Box();
  $box->setProperty("secret", "abcdefghijklmnopqrs");        // Noncompliant
  $box->setProperty("abcdefghijklmnopqrs", "secret");        // Noncompliant
  $box->setProperty('abcdefghijklmnopqrs', 'secret');        // Noncompliant
  $box->setProperty("secret", PASSED);                       // FN unable to resolve the value
  $box->setProperty(PASSED, "secret");                       // FN unable to resolve the value
  $box->setProperty("secret", "X");
  $box->setProperty("secret", "");
  $box->setProperty("secret", null);
  $box->setProperty("secret", 42);
  $box->setProperty("secret", 'auth');
}

class Box
{
  function setProperty($arg1, $arg2)
  {
    // do nothing
  }
}
