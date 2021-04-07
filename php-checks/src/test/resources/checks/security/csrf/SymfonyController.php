<?php

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Bundle\FrameworkBundle\Controller\Controller;

class MyController1 extends AbstractController
{
  public function action()
  {
    $this->createForm('', null, [
      'other_option' => false,
      'csrf_protection' => false, // Noncompliant {{Make sure disabling CSRF protection is safe here.}}
    //^^^^^^^^^^^^^^^^^^^^^^^^^^
    ]);

    $this->createForm('', null, array(
      'csrf_protection' => false, // Noncompliant
    ));

    $csrf = false;
//  ^^^^^^^^^^^^^> {{Setting variable to false.}}
    $this->createForm('', null, array(
      'csrf_protection' => $csrf, // Noncompliant {{Make sure disabling CSRF protection is safe here.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^
    ));

    if (foo()) {
      $csrfOption = false;
    } else {
      $csrfOption = true;
    }
    $this->createForm('', null, array(
      'csrf_protection' => $csrfOption,
    ));

    $this->createForm('', null, ['csrf_protection' => true]);
    $this->createForm('', null, ['other_option' => false]);
    $this->createForm('', null);
    $this->createForm('', null, $options);

    $this->redirectToRoute('/');
  }
}

class MyController2 extends Controller
{
  public function action()
  {
    $this->createForm('', null, [
      'csrf_protection' => false, // Noncompliant
    ]);
  }
}

class OtherClass
{
  public function action()
  {
    $this->createForm('', null, ['csrf_protection' => false,]);
  }
}

createForm('', null, [
  'csrf_protection' => false,
]);
