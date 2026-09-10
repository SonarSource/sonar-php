<?php

class MyClass
{
    private const CONST_PRIVATE = 'Private CONST';
}

function testClassConstantsAreDefined(): void
{
    $reflection = new \ReflectionClass(Subway::class);
    $constants = $reflection->getConstants();

    assert(in_array('CONST_PRIVATE', $constants, true), 'CONST_PRIVATE is not defined in the class');
}
