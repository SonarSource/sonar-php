<?php

/**
 * Comment for code -> raise no issue
 * $a = 1;
 */
$a = 2;

//Comment for code -> raise no issue
// $a = 1;
$a = 2;

/**
 * $a = 1;
 */
$a = 2;

// $a = 1;
$a = 2;

/**
 *
 */
$a = 2;

//
$a = 2;


class Foo {
  /** public $bar = null;  */

  // public $bar = null;

  public function bar() {
    // comment
    // $this->foo();

    // $this->foo();
  }
}


/**
 * @return {String}
 */
function sayHello() { }


// Block of commented out code with comment
//  function f () {
//    dosomething();
//  }

# Block of commented out code with comment with different token
//  function f () {
//    dosomething();
//  }

//  function f () {
      //    dosomething();
         //  }

// Set fonts
$helper->log('Set fonts');
$spreadsheet->getActiveSheet()->getStyle('A7:B7')->getFont()->setBold(true);
//$spreadsheet->getActiveSheet()->getStyle('B7')->getFont()->setBold(true);
// Set header and footer. When no different headers for odd/even are used, odd header is assumed.
$helper->log('Set header/footer');

/*
 * https://github.com/symfony/symfony/issues/4480
 */

/*class Foo {
  const BAR = 1;
}*/

/**
* class Foo {
*   const BAR = 1;
* }
*/

// namespace Foo\Bar;

// echo $x;
// echo $y;
// echo $z;

//php:S115 Types should be named in camel case

// use Foo\Bar;

// user-defined; not supported by PhpSpreadsheet

// use Foo\Bar\{ Bla, Bli };

/**
* # misleading opener -> is not commented out code
* @xyz
* @foobar
* @return void
*/

/**
* // misleading opener -> is not commented out code
* @xyz
* @foobar
* @return void
*/

/**
 * ## This should not raise an issue as it is used as comment extraction
 * for OpenAPI
 *
 * @OA\Get(
 *     path="/api/some/endpoint",
 *     operationId="getSomeEndpoint",
 *     tags={"Example"},
 *     summary="Does something",
 *     description="One-line markdown is not fun"
 *     @OA\Response(
 *         response=201,
 *         description="Successfully succeeded"
 *     )
 * )
 *
 * @return void
 */
