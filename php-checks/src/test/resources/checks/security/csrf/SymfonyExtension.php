<?php

namespace TestBundle\DependencyInjection;

use Symfony\Component\HttpKernel\DependencyInjection\Extension;
use Symfony\Component\DependencyInjection\ContainerBuilder;
use Symfony\Component\DependencyInjection\Extension\PrependExtensionInterface;

class TestExtension extends Extension implements PrependExtensionInterface
{
  public function prepend(ContainerBuilder $container)
  {

    $container->prependExtensionConfig('framework', ['csrf_protection' => false,]);// Noncompliant
//                                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^
    $container->prependExtensionConfig('framework', ['something_else' => true, 'csrf_protection' => false,]);// Noncompliant
    $container->prependExtensionConfig('framework', ['csrf_protection' => true,]);
    $container->prependExtensionConfig('framework', ['csrf_protection' => null,]);
    $container->prependExtensionConfig('something_else', ['csrf_protection' => false,]);

    $csrfOption = false;
//  ^^^^^^^^^^^^^^^^^^^> {{Setting variable to false.}}
    $container->prependExtensionConfig('framework', ['csrf_protection' => $csrfOption,]);// Noncompliant {{Make sure disabling CSRF protection is safe here.}}
//                                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    $container->loadFromExtension('framework', ['csrf_protection' => false,]); // Noncompliant
    $container->loadFromExtension('framework', ['csrf_protection' => null,]);
  }
}
