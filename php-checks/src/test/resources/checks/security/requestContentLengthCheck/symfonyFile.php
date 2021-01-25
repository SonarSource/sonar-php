<?php

namespace App\Entity;

use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Component\Validator\Mapping\ClassMetadata;

class Test
{
    public static function loadValidatorMetadata(ClassMetadata $metadata)
    {
        $metadata->addPropertyConstraint('test1', new Assert\File([
            'maxSize' => 8000000, // Compliant
        ]));

        $metadata->addPropertyConstraint('test2', new Assert\File([
            'maxSize' => '8000k', // Compliant
        ]));

        $metadata->addPropertyConstraint('test3', new Assert\File([
            'maxSize' => '8M', // Compliant
        ]));

        $metadata->addPropertyConstraint('test4', new Assert\File([
            'maxSize' => '7812Ki', // Compliant
        ]));

        $metadata->addPropertyConstraint('test5', new Assert\File([
            'maxSize' => '7Mi', // Compliant
        ]));

        $metadata->addPropertyConstraint('test6', new Assert\File([
            'maxSize' => '9M', // Noncompliant
        ]));

        $metadata->addPropertyConstraint('test7', new Assert\File([
            'maxSize' => '8Mi', // Noncompliant
        ]));

        $metadata->addPropertyConstraint('test8', new Assert\File([])); // Noncompliant
    }
}
