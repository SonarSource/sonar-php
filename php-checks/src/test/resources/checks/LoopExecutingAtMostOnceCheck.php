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
    return;
    while ($i < 10) { // Noncompliant
        echo $i;
        $i++;
        goto label;
    }

    for ($i = 0; $i < 10; $i++) { // Noncompliant
        break;
    }

    for ($i = 0; $i < 10;) { // Noncompliant
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

    while ($i < 10) {
        try {
            return foo();
        } catch (Exception $e) {

        }
    }

    foreach ($arr as $item) {
        return $item;
    }
}

function switch_for_coverage($i)
{
    switch ($i) {
        case 1:
            break;
    }
}

function for_loop_without_update($end, $cond)
{
    for ($i = 0; $i < $end;) {
       if ($cond)
           break;
    }
}

for ($i = 0; $i < 10; $i++) {
    for ($j = 0; $j < 10; $j++) { // Noncompliant
        break;
    }
}

for ($n = 0; $n < $period; ++$n) {

    switch ($period - $n) {
        case 1    :
            return round($cost * 0.5, 0);
            break;
    }
    $cost -= $fNRate;
}

function dont_check_break_in_switch() {
    while ($cond) { // Noncompliant
//  ^^^^^
        break;
//      ^^^^^^<
        switch ($i) {
            case 1:
                echo $i;
                break;  // break is not secondary location bc it's not breaking loop
        }
    }
}

function while_true() {
    while (true) {
        foo();
        break;
    }
}

function while_try()
{
    while ($cond) {
        try {
            foo();
            break;
        } catch (\Exception $e) {
        }
    }
}
