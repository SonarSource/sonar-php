<?php


function foo($i)
{
    while ($i < 10) { // Noncompliant {{Refactor this loop to do more than one iteration.}}
//  ^^^^^
        echo $i;
        $i++;
        break;
//      ^^^^^^< {{loop exit}}
    }

    while ($i < 10) { // Noncompliant
//  ^^^^^
        if ($i == 5) {
            break;
//          ^^^^^^< {{loop exit}}
        } else {
            return;
//          ^^^^^^^< {{loop exit}}
        }
    }

    while ($i < 10) { // Noncompliant
        echo $i;
        $i++;
        throw $error;
    }

    label:
    while ($i < 10) { // Noncompliant
        echo $i;
        $i++;
        goto label;
    }

    for ($i = 0; $i < 10; $i++) { // Noncompliant
        break;
    }

    for (; ;) { // Noncompliant
        break;
    }

    do {      // Noncompliant
        bar();
        break;
    } while (foo());

    while ($i < 10): // Noncompliant
        break;
    endwhile;

    for ($i = 1; $i < 10; $i++): // Noncompliant
        break;
    endfor;

    foreach ($arr as $item) { // Noncompliant
        break;
    }

    foreach ($arr as $item) { // Noncompliant
        foo();
        return $item;
    }

    foreach ($arr as $item):  // Noncompliant
        foo();
        return $item;
    endforeach;

}

class A
{
    public function foo()
    {
        while ($i < 10) { // Noncompliant
            break;
        }
    }
}

// script tree

while ($i < 10) { // Noncompliant
    break;
}

// function expression

$funexpr = function ($i) {
    while ($i < 10) { // Noncompliant
        break;
    }
};

function compliant()
{
    while (foo()) {
        bar();
        if (baz()) {
            return;
        }
    }

    for ($i = 0; $i < 10; $i++) {
        continue;
    }

    for ($i = 0; $i < 10; $i++) {
        if ($i == 5) break;
    }

    foreach ($arr as $item) {
        return $item;
    }

    foreach ($arr as $key => $value) {
        return $key;
    }

    foreach ($arr as $item):
        return $item;
    endforeach;

    foreach ($arr as $item):
    endforeach;

    foreach ($arr as $item) return $item;

    // FP because not handling try-catch with return properly
    while ($i < 10) { // Noncompliant
        try {
            return foo();
        } catch (Exception $e) {

        }
    }
}

function switch_for_coverage($i)
{
    switch ($i) {
        case 1:
            break;
    }
}
