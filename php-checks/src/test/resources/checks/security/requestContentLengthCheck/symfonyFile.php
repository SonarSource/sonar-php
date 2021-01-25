<?php

namespace App\Entity;

use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Component\Validator\Mapping\ClassMetadata;

class Test
{
    public static function loadValidatorMetadata(ClassMetadata $metadata)
    {
        $metadata->addPropertyConstraint('test', new Assert\File([
            'maxSize' => '9M', // Noncompliant {{Make sure the content length limit is safe here.}}
            //           ^^^^
        ]));

        $metadata->addPropertyConstraint('test', new Assert\File([
            'maxSize' => '8Mi', // Noncompliant
        ]));

        $metadata->addPropertyConstraint('test', new Assert\File([])); // Noncompliant
                                                              // ^^

        $metadata->addPropertyConstraint('test', new Assert\File([
            'maxSize' => null, // Noncompliant
        ]));

        $metadata->addPropertyConstraint('test', new Assert\File([
            'maxSize' => 8000000, // Compliant
        ]));

        $metadata->addPropertyConstraint('test', new Assert\File([
            'maxSize' => '8000k', // Compliant
        ]));

        $metadata->addPropertyConstraint('test', new Assert\File([
            'maxSize' => '8M', // Compliant
        ]));

        $metadata->addPropertyConstraint('test', new Assert\File([
            'maxSize' => '7812Ki', // Compliant
        ]));

        $metadata->addPropertyConstraint('test', new Assert\File([
            'maxSize' => '7Mi', // Compliant
        ]));

        $metadata->addPropertyConstraint('test', new Assert\File([
           'maxSize' => $unknown, // Compliant
        ]));

        $metadata->addPropertyConstraint('test', new Assert\File([
            'maxSize' => 'UnknownFormat', // Compliant
        ]));

        $metadata->addPropertyConstraint('test', new Assert\File()); // Compliant

        $metadata->addPropertyConstraint('test', new NotFileConstraint(['maxSize' => '7Mi'])); // Compliant
        $metadata->addPropertyConstraint('test', new NotFileConstraint); // Compliant
        $metadata->addPropertyConstraint('test', new $unknown()); // Compliant

        $metadata->addPropertyConstraint('test', new Assert\File($unknown)); // Compliant
    }
}
