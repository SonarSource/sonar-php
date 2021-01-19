<?php

namespace App\Http\Middleware;

use Illuminate\Foundation\Http\Middleware\VerifyCsrfToken as Middleware;

class VerifyCsrfToken1 extends Middleware
{
  protected $except = [ // Noncompliant {{Make sure disabling CSRF protection is safe here.}}
    'stripe/*',
    'http://example.com/foo/bar',
    'http://example.com/foo/*',
  ];

  protected $foo = [
    'stripe/*',
  ];
}

class VerifyCsrfToken2 extends Middleware
{
  protected $except = [];
}

class VerifyCsrfToken3 extends Middleware
{
  protected $except = [""];
}

class VerifyCsrfToken4 extends Middleware
{
  protected $except = [$unkown]; // Noncompliant
}

class VerifyCsrfToken5 extends Middleware
{
  protected $except;
}

class VerifyCsrfToken6 extends Middleware
{
  protected $except = "foo";
}

class VerifyCsrfToken7 extends OtherClass
{
  protected $except = [
  ];
}
