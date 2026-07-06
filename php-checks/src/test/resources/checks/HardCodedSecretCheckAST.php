<?php

// Global Constants
const PASSED = "Qm7vXpLr2FzT9baWtHx";   // Compliant nothing to do with secrets

// Noncompliant@+1 {{'SECRET' detected in this expression, review this potentially hard-coded secret.}}
const MY_SECRET = "Qm7vXpLr2FzT9baWtHx";
//                ^^^^^^^^^^^^^^^^^^^^^

// Noncompliant@+1 {{'api_key' detected in this expression, review this potentially hard-coded secret.}}
const api_key = 'Qm7vXpLr2FzT9baWtHx';

// Noncompliant@+1
const ABC = "Qm7vXpLr2FzT9baWtHx", AUTH = "Qm7vXpLr2FzT9baWtHx", XYZ = "Qm7vXpLr2FzT9baWtHx";
//                                        ^^^^^^^^^^^^^^^^^^^^^

// Noncompliant@+2
const TOKEN_HEREDOC = <<<END
Qm7vXpLr2FzT9baWtHx
END;

// FN it is literal tree and contains new lines
const TOKEN_NOWDOC = <<<'EOD'
Qm7vXpLr2FzT9baWtHx
EOD;

define("AUTH", "Qm7vXpLr2FzT9baWtHx");                    // Noncompliant

define("credential", "Qm7vXpLr2FzT9baWtHx");              // Noncompliant

define("namespace\level\Token", "Qm7vXpLr2FzT9baWtHx");   // Noncompliant

define("Qm7vXpLr2FzT9baWtHx");    // Compliant

// Variables declarations (as AssignmentExpression)
$var_ok = "Qm7vXpLr2FzT9baWtHx";  // Compliant

$oauth = "Qm7vXpLr2FzT9baWtHx";   // Noncompliant

// Variables in function
function do_something(): void
{
  $a = "Qm7vXpLr2FzT9baWtHx";         // Compliant
  $apikey = "Qm7vXpLr2FzT9baWtHx";    // Noncompliant
}

// Class Constants (as Variables in AST)
class ContainsConstrants
{
  // Noncompliant@+1
  const Token = "Qm7vXpLr2FzT9baWtHx";
//              ^^^^^^^^^^^^^^^^^^^^^

  const API_KEY = "Qm7vXpLr2FzT9baWtHx";  // Noncompliant
  const AUTH = "Qm7vXpLr2FzT9baWtHx";               // Noncompliant

  const OK_CONSTANT = "Qm7vXpLr2FzT9baWtHx";        // Compliant

  // Assignments
  function doSomething()
  {
    $someSecret = "";
    $someSecret = "Qm7vXpLr2FzT9baWtHx";          // Noncompliant

  }
}

// Class Properties
class ContainsProperties
{
  public $passed = 'Qm7vXpLr2FzT9baWtHx';
  public $apikey = 'Qm7vXpLr2FzT9baWtHx';                   // Noncompliant
  public string $api_key = "Qm7vXpLr2FzT9baWtHx";           // Noncompliant

  protected $credential = "Qm7vXpLr2FzT9baWtHx";            // Noncompliant
  private $private_credential = "Qm7vXpLr2FzT9baWtHx";      // Noncompliant
  static $static_credential = "Qm7vXpLr2FzT9baWtHx";        // Noncompliant

  public function __construct(
    public readonly string $auth = "Qm7vXpLr2FzT9baWtHx",             // Noncompliant
    protected              $protected_auth = "Qm7vXpLr2FzT9baWtHx",   // Noncompliant
    private                $private_auth = "Qm7vXpLr2FzT9baWtHx",     // Noncompliant
                           $oauth = "Qm7vXpLr2FzT9baWtHx",            // Noncompliant
                           $empty_auth
  )
  {
    // empty
  }

}

// Object
$object = (object)[
  'propertyOne' => 'Qm7vXpLr2FzT9baWtHx',
  'secret' => 'Qm7vXpLr2FzT9baWtHx',        // Noncompliant
  "token" => "Qm7vXpLr2FzT9baWtHx"          // Noncompliant
];

// Array
$array = array(
  'passed' => 'Qm7vXpLr2FzT9baWtHx',
  'auth' => 'Qm7vXpLr2FzT9baWtHx',          // Noncompliant
  "credential" => "Qm7vXpLr2FzT9baWtHx"     // Noncompliant
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
  if ($auth == "Qm7vXpLr2FzT9baWtHx")                     // Noncompliant
  {
    echo $auth;
  }
  if ("Qm7vXpLr2FzT9baWtHx" == $auth)                     // Noncompliant
  {
    echo $auth;
  }
  if ($auth === "Qm7vXpLr2FzT9baWtHx")                     // Noncompliant
  {
    echo $auth;
  }
  if ("Qm7vXpLr2FzT9baWtHx" === $auth)                     // Noncompliant
  {
    echo $auth;
  }
  if ($auth <=> "Qm7vXpLr2FzT9baWtHx")                     // Noncompliant
  {
    echo $auth;
  }
  if ("Qm7vXpLr2FzT9baWtHx" <=> $auth)                     // Noncompliant
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
  if (strcmp($auth, "Qm7vXpLr2FzT9baWtHx"))         // Noncompliant
  {
    echo $auth;
  }
  if (strcmp("Qm7vXpLr2FzT9baWtHx", $auth))         // Noncompliant
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
  if (strcmp("Qm7vXpLr2FzT9baWtHx"))
  {
    echo $auth;
  }
  if (strcmp("Qm7vXpLr2FzT9baWtHx", $auth, "abc"))
  {
    echo $auth;
  }

  // case insensitive
  if (strcasecmp($auth, "Qm7vXpLr2FzT9baWtHx"))    // Noncompliant
  {
    echo $auth;
  }
  if (strcasecmp("Qm7vXpLr2FzT9baWtHx", $auth))    // Noncompliant
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
  if (strcasecmp("Qm7vXpLr2FzT9baWtHx"))
  {
    echo $auth;
  }
  if (strcasecmp("Qm7vXpLr2FzT9baWtHx", $auth, "abc"))
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
  some2ArgsFunction("secret", "Qm7vXpLr2FzT9baWtHx");        // Noncompliant
  some2ArgsFunction("Qm7vXpLr2FzT9baWtHx", "secret");        // Noncompliant
  some2ArgsFunction('Qm7vXpLr2FzT9baWtHx', 'secret');        // Noncompliant
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
  $box->setProperty("secret", "Qm7vXpLr2FzT9baWtHx");        // Noncompliant
  $box->setProperty("Qm7vXpLr2FzT9baWtHx", "secret");        // Noncompliant
  $box->setProperty('Qm7vXpLr2FzT9baWtHx', 'secret');        // Noncompliant
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
  $variable2 = "login=a&secret=Qm7vXpLr2FzT9baWtHx"; // Noncompliant
  $variable3 = "login=a&token=Qm7vXpLr2FzT9baWtHx"; // Noncompliant
  $variable4 = "login=a&api_key=Qm7vXpLr2FzT9baWtHx"; // Noncompliant
  $variable5 = "login=a&api.key=Qm7vXpLr2FzT9baWtHx"; // Noncompliant
  $variable6 = "login=a&api-key=Qm7vXpLr2FzT9baWtHx"; // Noncompliant
  $variable7 = "login=a&credential=Qm7vXpLr2FzT9baWtHx"; // Noncompliant
  $variable8 = "login=a&auth=Qm7vXpLr2FzT9baWtHx"; // Noncompliant
  $variable9 = "login=a&secret=";
  $variableA = "login=a&secret= ";
  $variableB = "secret=&login=Qm7vXpLr2FzT9baWtHx"; // Compliant
  $variableC = "Okapi-key=42, Okapia Johnstoni, Forest/Zebra Giraffe"; // Compliant
  $variableD = "gran-papi-key=Known by everybody in the world like PWD123456"; // Compliant

// FN
  $variableE = <<<END
      <form action="/delete?secret=Qm7vXpLr2FzT9baWtHx">
        <input type="text" id="item" value="42"><br><br>
        <input type="submit" value="Delete">
      </form>
  END;

// Noncompliant@+1
  $variableF = <<<'END'
      <form action="/delete?secret=Qm7vXpLr2FzT9baWtHx">
        <input type="text" id="item" value="42"><br><br>
        <input type="submit" value="Delete">
      </form>
  END;

  // Secrets starting with "?", ":", "\"", containing "%s" or with less than 2 characters are ignored
  $query1 = "secret=?Qm7vXpLr2FzT9baWtHx"; // Compliant
  $query1_1 = "secret=???"; // Compliant
  $query1_2 = "secret=X"; // Compliant
  $query1_3 = "secret=anonymous"; // Compliant
  $query4 = "secret='" + $secret + "'"; // Compliant
  $query2 = "secret=:password"; // Compliant
  $query3 = "secret=:param"; // Compliant
  $query5 = "secret=%s"; // Compliant
  $query6 = "secret=\"%s\""; // Compliant
  $query7 = "\"secret=\""; // Compliant
  $query8 = "secret=:Qm7vXpLr2FzT9baWtHx"; // Compliant
  $query9 = "secret=%s_Qm7vXpLr2FzT9baWtHx"; // Compliant


  $params1 = "user=admin&secret=Secret9384756201938475"; // Compliant, "Secret" recognized as a fake-value word by SecretClassifier
  $params2 = "secret=no\nuser=admin0123456789"; // Compliant
  $sqlserver1= "pgsql:host=localhost port=5432 dbname=test user=postgres secret=Qm7vXpLr2FzT9baWtHx"; // Noncompliant
  $sqlserver2 = "pgsql:host=localhost port=5432 dbname=test secret=no user=Qm7vXpLr2FzT9baWtHx"; // Compliant

  // Spaces and & are not included into the token, it shows us the end of the token.
  $params3 = "token=Qm7vXpLr2FzT9baWtHx user=admin"; // Noncompliant
  $params4 = "token=Qm7vXpLr2FzT9baWtHx&user=admin"; // Noncompliant
  $params5 = "token=987654&Qm7vXpLr2FzT9baWtHx"; // Compliant, FN, even if "&" is accepted in a password, it also indicates a cut in a string literal
  $params6 = "token=987654:Qm7vXpLr2FzT9baWtHx"; // Noncompliant
  $params7 = "token= Qm7vXpLr2FzT9baWtHx"; // Noncompliant
  $params8 = "token=Qm7vXpLr2FzT9baWtHx;Qm7vXpLr2FzT9baWtHxaaa"; // Noncompliant
  $params9 = "token=abc;Qm7vXpLr2FzT9baWtHxaaa"; // Compliant, ";" indicates a cut in string literal
  $paramsA = "token=abc#Qm7vXpLr2FzT9baWtHxaaa"; // Compliant, "#" indicates a cut in string literal
  $paramsB = "token=abc,Qm7vXpLr2FzT9baWtHxaaa"; // Compliant, "," indicates a cut in string literal
  $paramsC = "token=abc|Qm7vXpLr2FzT9baWtHxaaa"; // Compliant, "|" indicates a cut in string literal

  // URLs are reported by S2068 only.
  $url = "http://user:123456@server.com/path";

  $secret001 = "sk_live_xf2fh0Hu3LqXlqqUg2DEWhEz"; // Noncompliant
  $secret002 = "examples/commit/16ad89c4172c259f15bce56e";
  $secret003 = "examples/commit/8e1d746900f5411e9700fea0"; // Compliant, "examples" recognized as a fake-value word by SecretClassifier
  $secret004 = "examples/commit/revision/469001e9700fea0";
  $secret005 = "xml/src/main/java/org/xwiki/xml/html/file";
  $secret006 = "abcdefghijklmnop"; // Compliant
  $secret007 = "Zk4mPq8Vr2Wn5TySb"; // Noncompliant
  $secret008 = "3fB9e7Dc2A6b81f5E"; // Noncompliant
  $secret009 = "098765432198765432109876543210"; // Noncompliant
  $secret010 = "QmVpLrFzTbWtHxNkYcSaJdReUiOp"; // Noncompliant
  $secret011 = "012345670123456701234567012345";
  $secret012 = "987654321098765432109876543210"; // Noncompliant
  $secret013 = "234.167.076.123";
  $ip_secret1 = "bfee:e3e1:9a92:6617:02d5:256a:b87a:fbcc"; // Compliant: ipv6 format
  $ip_secret2 = "2001:db8:1::ab9:C0A8:102"; // Compliant: ipv6 format
  $ip_secret3 = "::ab9:C0A8:102"; // Compliant: ipv6 format
  $secret015 = "org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH";

  // Example of Telegram bot token
  $secret016 = "bot987654:ABC-DEF9876ghIkl-zyx57W2v1u987ew11"; // Noncompliant
  // Secret with "&"
  $secret017 = "012&987654012987654012987&654012"; // Noncompliant
  $secret018 = "&98&765401298765401298&76540&"; // Noncompliant

  // Now filtered because the value contains the secret word, recognized as a fake-value word by SecretClassifier.
  $secret019 = "Secret_9384756201938475"; // Compliant
  $secret020 = "secret_9384756201938475"; // Compliant

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
  $SECRET_VAULT_REF = "op:/x9F2kLpQ7vRtYcWa1"; // Compliant, SecretClassifier recognizes vault reference syntax

  // Backslashes are filtered further:
  // \n, \t, \r, \" are excluded
  $secretWithBackSlashes = "abcdefghij\nklmnopqrs"; // Compliant
  $secretWithBackSlashes2 = "abcdefghij\tklmnopqrs"; // Compliant
  $secretWithBackSlashes3 = "abcdefghij\rklmnopqrs"; // Compliant
  $secretWithBackSlashes4 = "abcdefghij\"klmnopqrs"; // Compliant
  // When the secret is starting or ending with a backslash
  $secretWithBackSlashes5 = "\\Qm7vXpLr2FzT9baWtHx"; // Compliant
  $secretWithBackSlashes6 = "Qm7vXpLr2FzT9baWtHx\\"; // Compliant
  // When the secret is starting with =
  $secretWithBackSlashes7 = "=Qm7vXpLr2FzT9baWtHx";
  // = in the middle or end is okay
  $secretWithBackSlashes8 = "Qm7vXpLr2FzT9baWtHx="; // Noncompliant
  $secretWithBackSlashes9 = "Qm7vXpLr2FzT9baWtHx=="; // Noncompliant
  $secretWithBackSlashes10 = "Qm7vXpLr2Fz=T9baWtHx"; // Noncompliant

  // Only [a-zA-Z0-9_.+/~$-] are accepted as secrets characters
  $OkapiKeyboard = "what a strange QWERTY keyboard for animals"; // Compliant
  $OKAPI_KEYBOARD = "what a strange QWERTY keyboard for animals"; // Compliant
  $okApiKeyValue = "Spaces are UNEXPECTED 012 345 678"; // Compliant
  $tokenism = "(Queen's Partner's Stored Knowledge is a Minimal Sham)"; // Compliant
  $tokenWithExcludedCharacters2 = "abcdefghij|klmnopqrs"; // Compliant

  // "anonymous" needs to be ignored
  $fieldNameWithSecretInIt = "anonymous";
}
