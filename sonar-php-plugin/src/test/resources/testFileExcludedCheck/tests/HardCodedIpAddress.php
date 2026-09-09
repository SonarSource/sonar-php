<?php

function testUsingAnIP(): void
{
    $serverAddress = "1.20.33.145";

    assert(str_contains($serverAddress, "33"), 'serverAddress is not defined correctly');
}
