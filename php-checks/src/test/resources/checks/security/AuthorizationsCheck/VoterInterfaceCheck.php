<?php
use Symfony\Component\Security\Core\Authorization\Voter\VoterInterface;

class NoncompliantSimpleExample implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        return self::ACCESS_GRANTED; // Noncompliant
    }
}

class CompliantSimpleExample implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        return self::ACCESS_ABSTAIN; // Compliant
    }
}

class A extends NoncompliantSimpleExample
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        return self::ACCESS_ABSTAIN; // Compliant
    }
}

class B extends CompliantSimpleExample
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        return 42; // Noncompliant
    }
}

class ReturnStringLiteral implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        return "ACCESS_ABSTAIN"; // Noncompliant
    }
}

class ReturnNullLiteral implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        return null; // Noncompliant
    }
}

class ReturnNumericLiteral implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        return 42; // Noncompliant
    }
}

class ReturnUnknownFunctionResult implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        return foo(); // Compliant
    }
}

class ReturnUnknownVariable implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        return $foo; // Compliant
    }
}

class ReturnResolvableVariableNoncompliant implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        $a = 42;
        return $a; // Noncompliant
    }
}

class ReturnResolvableVariableCompliant implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        $a = self::ACCESS_ABSTAIN;
        return $a; // Compliant
    }
}

class ReturnResolvableVariableCompliant implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        $a = self::ACCESS_ABSTAIN;
        return $a; // Compliant
    }
}

class ReturnResponseAllowFunctionCall extends VoterInterface
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return Response::allow(); // Compliant
    }
}

class ReturnResponseDenyFunctionCall extends VoterInterface
{

    public function voteOnAttribute(TokenInterface $token, $subject, array $attributes)
    {
        return Response::deny(); // Compliant
    }
}

class NotRelevantMethod implements VoterInterface
{

    public function notRelevantMethod(TokenInterface $token, $subject, array $attributes)
    {
        return self::ACCESS_GRANTED; // Compliant
    }
}

class MultipleNoncompliantReturns implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        if (foo()) {
            return self::ACCESS_GRANTED;
        } else if (bar()) {
            return 2;
        }
        return true; // Noncompliant
    }

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        if (foo()) {
            return self::ACCESS_GRANTED;
        } else if (bar()) {
            return self::ACCESS_DENIED;
        }
        return true; // Compliant
    }
}

class MultipleReturnsAtLeastOneIsCompliant implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        if (foo()) {
            return self::ACCESS_DENIED;
        }
        return self::ACCESS_GRANTED; // Compliant
    }
}

class AnotherMultipleReturnsAtLeastOneIsCompliant implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        if (foo()) {
            return self::ACCESS_GRANTED;
        } else if (bar()) {
            return self::ACCESS_DENIED;
        }
        return true;
    }
}

class ReturnNothing implements VoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes) // Noncompliant
    {
        $foo = 42;
    }
}

class DoesNotExtendVoterInterface
{

    public function vote(TokenInterface $token, $subject, array $attributes)
    {
        return self::ACCESS_GRANTED; // Compliant
    }
}

abstract class AbstractExample implements VoterInterface
{

    abstract function vote(TokenInterface $token, $subject, array $attributes);
}
