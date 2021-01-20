<?php
use Illuminate\Support\Facades\Gate;

class NoncompliantGuard
{

    public function NonCompliantSimpleExamples()
    {
        Gate::define('xxx', function ($user) {
            return true; // Noncompliant
        });

        Gate::before(function ($user, $ability) {
            return true; // Noncompliant
        });

        Gate::after(function ($user, $ability, $result, $arguments) {
            return TRUE; // Noncompliant
        });
    }

    public function CompliantSimpleExamples()
    {
        Gate::define('xxx', function ($user) {
            return false; // Compliant
        });

        Gate::before(function ($user, $ability) {
            return FALSE; // Compliant
        });

        Gate::after(function ($user, $ability, $result, $arguments) {
            return null; // Compliant
        });
    }

    public function MultipleReturns()
    {
        Gate::define('xxx', function ($user) {
            if (bar()) {
                return 42;
            }
            return true; // Noncompliant
        });

        Gate::define('xxx', function ($user) {
            if (foo()) {
                return false;
            }
            return true; // Compliant
        });
    }

    public function ReturnFunctionCalls()
    {
        Gate::define('xxx', function ($user) {
            return Response::allow(); // Noncompliant
        });

        Gate::define('xxx', function ($user) {
            return Response::deny();
        });

        Gate::define('xxx', function ($user) {
            return bar(); // Compliant - the return value of bar() is unknown
        });
    }

    public function ReturnStringOrInt()
    {
        Gate::after(function ($user, $ability, $result, $arguments) {
            return "true"; // Noncompliant
        });

        Gate::define('xxx', function ($user) {
            return "false"; // Noncompliant
        });

        Gate::before(function ($user, $ability) {
            return "Null"; // Noncompliant
        });
    }

    public function ReturnVariable()
    {
        Gate::before(function ($user, $ability) {
            return $a; // Compliant
        });

        Gate::define('xxx', function ($user) {
            $a = null;
            return $a; // compliant
        });

        Gate::define('xxx', function ($user) {
            $a = false;
            return $a; // compliant
        });

        Gate::define('xxx', function ($user) {
            $a = true;
            return $a; // Noncompliant
        });

        Gate::after(function ($user, $ability, $result, $arguments) {
            $a = "null";
            return $a; // Noncompliant
        });
    }

    public function NotRelatedFunctionCalls()
    {
        $Gate::define('xxx', function ($user) {
            return true; // Compliant
        });

        Gate::$class('xxx', function ($user) {
            return true; // Compliant
        });

        Gate::define('xxx', $foo);

        Foo::define('xxx', function ($user) {
            return true; // Compliant
        });
    }
}
