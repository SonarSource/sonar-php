<?php


use Symfony\Component\Form\AbstractType;
use Symfony\Component\OptionsResolver\OptionsResolver;

class Type extends AbstractType
{
  public function configureOptions(OptionsResolver $resolver)
  {
    $resolver->setDefaults([
      'data_class'      => Type::class,
      'csrf_protection' => false // Noncompliant
    ]);

    $resolver->setDefaults([
      'csrf_protection' => false // Noncompliant
    ]);

    $resolver->setDefaults([
      'csrf_protection' => true // OK
    ]);

    $resolver->setDefaults([
      'data_class'      => Type::class,
    ]);

    $resolver->setDefaults($options);

    $csrf = false;
//  ^^^^^^^^^^^^^> {{Setting variable to false.}}
    $resolver->setDefaults([
      'csrf_protection' => $csrf // Noncompliant {{Make sure disabling CSRF protection is safe here.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^
    ]);


    if (foo()) {
      $csrfOption = false;
    } else {
      $csrfOption = true;
    }
    $resolver->setDefaults([
      'csrf_protection' => $csrfOption
    ]);

    $resolver->setDefaults([
      'csrf_protection' => $unknown
    ]);
  }
}

class OtherType extends OtherClass
{
  public function configureOptions(OptionsResolver $resolver)
  {
    $resolver->setDefaults([
      'data_class' => OtherType::class,
      'csrf_protection' => false
    ]);
  }
}
