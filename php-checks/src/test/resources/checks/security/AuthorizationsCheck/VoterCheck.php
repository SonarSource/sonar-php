<?php
use Symfony\Component\Security\Core\Authorization\Voter\Voter;


class ReturnResolvableNullVariableNoncompliant extends Voter
{
    
    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return 'EDIT' === $attribute;
    }
}


class NonCompliantSimpleExample extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return true; // Noncompliant
    }
}

class CompliantSimpleExample extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return false; // Compliant
    }
}

class A extends CompliantSimpleExample
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return TRUE; // Noncompliant
    }
}

class B extends NonCompliantSimpleExample
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return false; // Compliant
    }
}

class ReturnFalseStringLiteral extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return "false"; // Noncompliant
    }
}

class ReturnNullStringLiteral extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return "NULL"; // Noncompliant
    }
}

class ReturnNullLiteral extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return null; // Noncompliant
    }
}

class ReturnNumericLiteral extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return 42; // Noncompliant
    }
}

class ReturnUnknownFunctionResult extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return bar(); // Compliant - the return value of bar is not known
    }
}

class ReturnUnknownVariable extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return $a; // Compliant
    }
}

class ReturnResolvableFalseVariableCompliant extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        $a = FALSE;
        return $a; // Compliant
    }
}

class ReturnResolvableNullVariableNoncompliant extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        $a = null;
        return $a; // Noncompliant
    }
}

class ReturnResolvableVariableNoncompliant extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        $a = true;
        return $a; // Noncompliant
    }
}

class ReturnResolvableStringVariableFN extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        if (foo()) {
            $a = "false";
            return $a;
        } else {
            $a = "null";
            return $a; // Compliant - FN
        }
    }
}

class ReturnResponseAllowFunctionCall extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return Response::allow(); // Noncompliant
    }
}

class ReturnResponseDenyFunctionCall extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return Response::deny(); // Compliant
    }
}

class NotRelevantMethodExample extends Voter
{

    public function notRelevantMethod(TokenInterface $token, $subject, array $attributes)
    {
        return true; // Compliant
    }
}

class MultipleNonCompliantReturns extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        if (foo()) {
            return self::ACCESS_GRANTED;
        } else if (bar()) {
            return TRUE;
        }
        return 42; // Noncompliant
    }
}

class MultipleReturnsAtLeastOneIsCompliant extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        if (foo()) {
            return FALSE;
        }
        return true; // Compliant
    }
}

class ReturnNothing extends Voter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes) // Noncompliant
    {
        $foo = 42;
    }
}

class DoesNotExtendVoter
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return true; // Compliant
    }
}
