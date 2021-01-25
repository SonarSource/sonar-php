<?php

namespace core;

header("Access-Control-Allow-Origin: *"); // Noncompliant
header("Access-Control-Allow-Origin:* "); // Noncompliant
Header("access-control-allow-origin:     *   "); // Noncompliant
header(code:42, replace:true, header:"Access-Control-Allow-Origin: *"); // Noncompliant

header("Access-Control-Allow-Origin: " + $_SERVER['HTTP_ORIGIN']); // Compliant - FN
header("Access-Control-Allow-Origin: *foo*");
header("Access-Control-Allow-Origin", "*");
header("Other-Property: *"); // Compliant
header(); // Compliant

class A {
    function foo() {
        header("Access-Control-Allow-Origin: *"); // Noncompliant
        header("Access-Control-Allow-Origin: foo"); // Compliant
    }
}

namespace laravel;
use Illuminate\Http\Response;

$response = new Response();
$response->header('Access-Control-Allow-Origin', "*"); // Noncompliant
response()->header('Access-Control-Allow-Origin', "*"); // Noncompliant
$response->HEADER('  access-control-allow-origin  ', "  *  "); // Noncompliant
$response->header(values:"*", key:'Access-Control-Allow-Origin'); // Noncompliant

$response->header('Access-Control-Allow-Origin', "foo"); // Compliant
response()->header('Access-Control-Allow-Origin', "foo"); // Compliant
$response->header('Other-Property', "*"); // Compliant



class A {
    function foo() {
        $response = new Response();
        $response->header('Access-Control-Allow-Origin', "*"); // Noncompliant
        $response->header('Access-Control-Allow-Origin', "foo"); // Compliant
    }
}

namespace symfony;
use Symfony\Component\HttpFoundation\Response;

$response = new Response($bar, 42, ['Access-Control-Allow-Origin' => '*']); // Noncompliant
$response = new Response($bar, 42, Array('Access-Control-Allow-Origin' => '*')); // Noncompliant
$response = new response(headers:['  access-control-allow-origin  ' => '  *   '], status:42, content:$bar); // Noncompliant

$response = new Response($bar, 42, ['Access-Control-Allow-Origin' => 42]); // Compliant
$response = new Response($bar, 42, ['Other-Property' => '*']); // Compliant
$response = new Foo($bar, 42, ['Access-Control-Allow-Origin' => '*']); // Compliant

$response->headers->set('  access-control-allow-origin  ', '  *  '); // Noncompliant
$response->headers->Set(values:"*", key:'Access-Control-Allow-Origin'); // Noncompliant

$response->headers->set('Access-Control-Allow-Origin', 'foo'); // Compliant
$response->headers->set('Other-Property', '*'); // Compliant
$response->headers->foo('Access-Control-Allow-Origin', '*'); // Compliant
set('Access-Control-Allow-Origin', '*'); // Compliant

class A {
    function foo() {
        $response = new Response('Content', 42, ['Access-Control-Allow-Origin' => '*']); // Noncompliant
        $response = new Response('Content', 42, ['Access-Control-Allow-Origin' => "foo"]); // Compliant
    }
}

namespace foo;
// FPs - we don't control the type of the receiver
$foo = new Foo();
$foo->header('Access-Control-Allow-Origin', "*"); //Noncompliant
foo()->header('Access-Control-Allow-Origin', "*"); //Noncompliant
$foo->set('Access-Control-Allow-Origin', "*"); //Noncompliant
$foo->headers->set('Access-Control-Allow-Origin', "*"); //Noncompliant
$foo->foo->set('Access-Control-Allow-Origin', "*"); //Noncompliant
foo()->set('Access-Control-Allow-Origin', "*"); //Noncompliant
