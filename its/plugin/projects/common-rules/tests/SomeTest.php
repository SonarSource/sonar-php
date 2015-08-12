<?php

require_once dirname(__FILE__) . '/../src/Math.php';

class Example_MathTest extends PHPUnit_Framework_TestCase
{

    protected $math = null;

    public function setUp()
    {
        parent::setUp();

        $this->math = new Example_Math();
    }

    /**
     * Successful test.
     */
    public function testAddSuccess()
    {
        sleep(2);
        $this->assertEquals(4, $this->math->add(1, 3));
    }

    /**
     * Successful test.
     */
    public function testSubSuccess()
    {
        $this->assertEquals( -2, $this->math->sub( 1, 3 ) );
    }

    /**
     * Failing test.
     */
    public function testSubFail()
    {
        sleep(2);
        $this->assertEquals( 0, $this->math->sub( 2, 1 ) );
    }

    /**
     * Skipped test.
     */
    public function testSubSkip()
    {
        $this->arkTestSkipped('Skipped test');
    }
}
