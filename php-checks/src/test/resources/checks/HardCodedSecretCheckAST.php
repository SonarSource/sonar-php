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

define("abcdefghijklmnopqrs");    // Compliant

// Variables declarations (as AssignmentExpression)
$var_ok = "abcdefghijklmnopqrs";  // Compliant

$oauth = "abcdefghijklmnopqrs";   // Noncompliant

// Variables in function
function do_something(): void
{
  $a = "abcdefghijklmnopqrs";         // Compliant
  $apikey = "abcdefghijklmnopqrs";    // Noncompliant
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
    // empty
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
  if (strcmp())
  {
    echo $auth;
  }
  if (strcmp("abcdefghijklmnopqrs"))
  {
    echo $auth;
  }
  if (strcmp("abcdefghijklmnopqrs", $auth, "abc"))
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
  if (strcasecmp())
  {
    echo $auth;
  }
  if (strcasecmp("abcdefghijklmnopqrs"))
  {
    echo $auth;
  }
  if (strcasecmp("abcdefghijklmnopqrs", $auth, "abc"))
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

function detectSecretsInStrings($secret)
{
  $variable1 = "blabla";
  $variable2 = "login=a&secret=abcdefghijklmnopqrs"; // Noncompliant
  $variable3 = "login=a&token=abcdefghijklmnopqrs"; // Noncompliant
  $variable4 = "login=a&api_key=abcdefghijklmnopqrs"; // Noncompliant
  $variable5 = "login=a&api.key=abcdefghijklmnopqrs"; // Noncompliant
  $variable6 = "login=a&api-key=abcdefghijklmnopqrs"; // Noncompliant
  $variable7 = "login=a&credential=abcdefghijklmnopqrs"; // Noncompliant
  $variable8 = "login=a&auth=abcdefghijklmnopqrs"; // Noncompliant
  $variable9 = "login=a&secret=";
  $variableA = "login=a&secret= ";
  $variableB = "secret=&login=abcdefghijklmnopqrs"; // Compliant
  $variableC = "Okapi-key=42, Okapia Johnstoni, Forest/Zebra Giraffe"; // Compliant
  $variableD = "gran-papi-key=Known by everybody in the world like PWD123456"; // Compliant

// FN
  $variableE = <<<END
      <form action="/delete?secret=abcdefghijklmnopqrs">
        <input type="text" id="item" value="42"><br><br>
        <input type="submit" value="Delete">
      </form>
  END;

// Noncompliant@+1
  $variableF = <<<'END'
      <form action="/delete?secret=abcdefghijklmnopqrs">
        <input type="text" id="item" value="42"><br><br>
        <input type="submit" value="Delete">
      </form>
  END;

  // Secrets starting with "?", ":", "\"", containing "%s" or with less than 2 characters are ignored
  $query1 = "secret=?abcdefghijklmnopqrs"; // Compliant
  $query1_1 = "secret=???"; // Compliant
  $query1_2 = "secret=X"; // Compliant
  $query1_3 = "secret=anonymous"; // Compliant
  $query4 = "secret='" + $secret + "'"; // Compliant
  $query2 = "secret=:password"; // Compliant
  $query3 = "secret=:param"; // Compliant
  $query5 = "secret=%s"; // Compliant
  $query6 = "secret=\"%s\""; // Compliant
  $query7 = "\"secret=\""; // Compliant
  $query8 = "secret=:abcdefghijklmnopqrs"; // Compliant
  $query9 = "secret=%s_abcdefghijklmnopqrs"; // Compliant


  $params1 = "user=admin&secret=Secret0123456789012345678"; // Noncompliant
  $params2 = "secret=no\nuser=admin0123456789"; // Compliant
  $sqlserver1= "pgsql:host=localhost port=5432 dbname=test user=postgres secret=abcdefghijklmnopqrs"; // Noncompliant
  $sqlserver2 = "pgsql:host=localhost port=5432 dbname=test secret=no user=abcdefghijklmnopqrs"; // Compliant

  // Spaces and & are not included into the token, it shows us the end of the token.
  $params3 = "token=abcdefghijklmnopqrs user=admin"; // Noncompliant
  $params4 = "token=abcdefghijklmnopqrs&user=admin"; // Noncompliant
  $params5 = "token=123456&abcdefghijklmnopqrs"; // Compliant, FN, even if "&" is accepted in a password, it also indicates a cut in a string literal
  $params6 = "token=123456:abcdefghijklmnopqrs"; // Noncompliant

  // URLs are reported by S2068 only.
  $url = "http://user:123456@server.com/path";

  $secret001 = "sk_live_xf2fh0Hu3LqXlqqUg2DEWhEz"; // Noncompliant
  $secret002 = "examples/commit/16ad89c4172c259f15bce56e";
  $secret003 = "examples/commit/8e1d746900f5411e9700fea0"; // Noncompliant
  $secret004 = "examples/commit/revision/469001e9700fea0";
  $secret005 = "xml/src/main/java/org/xwiki/xml/html/file";
  $secret006 = "abcdefghijklmnop"; // Compliant
  $secret007 = "abcdefghijklmnopq"; // Noncompliant
  $secret008 = "0123456789abcdef0"; // Noncompliant
  $secret009 = "012345678901234567890123456789"; // Noncompliant
  $secret010 = "abcdefghijklmnopabcdefghijkl"; // Noncompliant
  $secret011 = "012345670123456701234567012345";
  $secret012 = "012345678012345678012345678012"; // Noncompliant
  $secret013 = "234.167.076.123";
  $ip_secret1 = "bfee:e3e1:9a92:6617:02d5:256a:b87a:fbcc"; // Compliant: ipv6 format
  $ip_secret2 = "2001:db8:1::ab9:C0A8:102"; // Compliant: ipv6 format
  $ip_secret3 = "::ab9:C0A8:102"; // Compliant: ipv6 format
  $secret015 = "org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH";

  // Example of Telegram bot token
  $secret016 = "bot123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11"; // Noncompliant
  // Secret with "&"
  $secret017 = "012&345678012345678012345&678012"; // Noncompliant
  $secret018 = "&12&345678012345678012345&67801&"; // Noncompliant

  // Don't filter when the secret is containing any of the secret word.
  $secret019 = "Secret_0123456789012345678"; // Noncompliant
  $secret020 = "secret_0123456789012345678"; // Noncompliant

  // Simple constants will be filtered thanks to the entropy check
  $SECRET_INPUT = "[id='secret']"; // Compliant
  $SECRET_PROPERTY = "custom.secret"; // Compliant
  $TRUSTSTORE_SECRET = "trustStoreSecret"; // Compliant
  $CONNECTION_SECRET = "connection.secret"; // Compliant
  $RESET_SECRET = "/users/resetUserSecret"; // Compliant
  $RESET_TOKEN = "/users/resetUserToken"; // Compliant
  $secretToChar = "secret".toCharArray(); // Compliant
  $secretToChar2 = "http-secret".toCharArray(); // Compliant
  $secretToString = "http-secret".toString(); // Compliant
  $secretFromGetSecret = getSecret(""); // Compliant
  $CA_SECRET = "ca-secret"; // Compliant
  $caSecret = $CA_SECRET; // Compliant

  // Backslashes are filtered further:
  // \n, \t, \r, \" are excluded
  $secretWithBackSlashes = "abcdefghij\nklmnopqrs"; // Compliant
  $secretWithBackSlashes2 = "abcdefghij\tklmnopqrs"; // Compliant
  $secretWithBackSlashes3 = "abcdefghij\rklmnopqrs"; // Compliant
  $secretWithBackSlashes4 = "abcdefghij\"klmnopqrs"; // Compliant
  // When the secret is starting or ending with a backslash
  $secretWithBackSlashes5 = "\\abcdefghijklmnopqrs"; // Compliant
  $secretWithBackSlashes6 = "abcdefghijklmnopqrs\\"; // Compliant
  // When the secret is starting with =
  $secretWithBackSlashes7 = "=abcdefghijklmnopqrs";
  // = in the middle or end is okay
  $secretWithBackSlashes8 = "abcdefghijklmnopqrs="; // Noncompliant
  $secretWithBackSlashes9 = "abcdefghijklmnopqrs=="; // Noncompliant
  $secretWithBackSlashes10 = "abcdefghij=klmnopqrs"; // Noncompliant

  // Only [a-zA-Z0-9_.+/~$-] are accepted as secrets characters
  $OkapiKeyboard = "what a strange QWERTY keyboard for animals"; // Compliant
  $OKAPI_KEYBOARD = "what a strange QWERTY keyboard for animals"; // Compliant
  $okApiKeyValue = "Spaces are UNEXPECTED 012 345 678"; // Compliant
  $tokenism = "(Queen's Partner's Stored Knowledge is a Minimal Sham)"; // Compliant
  $tokenWithExcludedCharacters2 = "abcdefghij|klmnopqrs"; // Compliant

  // "anonymous" needs to be ignored
  $fieldNameWithSecretInIt = "anonymous";
}
