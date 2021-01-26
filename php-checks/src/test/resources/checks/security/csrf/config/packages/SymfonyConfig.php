<?php

namespace Symfony\Component\DependencyInjection\Loader\Configurator;

return static function (ContainerConfigurator $container) {
  // Symfony\Component\DependencyInjection\Loader\Configurator\ParametersConfigurator::set
  $container->parameters()->set('csrf_protection', false); // Noncompliant {{Make sure disabling CSRF protection is safe here.}}
  $container->parameters()->set('csrf_protection', true);
  $container->parameters()->set('csrf_protection', null);
  $name = 'csrf_protection';
  $container->parameters()->set($name, false); // Noncompliant

  $csrfParam = false;
//^^^^^^^^^^^^^^^^^^> {{Setting variable to false.}}
  $container->parameters()->set('csrf_protection', $csrfParam); // Noncompliant {{Make sure disabling CSRF protection is safe here.}}
//                                                 ^^^^^^^^^^
  $container->parameters()->set('something_else', null);

  $container->extension('framework', ['csrf_protection' => false,]);// Noncompliant
//                                    ^^^^^^^^^^^^^^^^^^^^^^^^^^
  $container->extension('framework', ['something_else' => true, 'csrf_protection' => false,]);// Noncompliant
  $container->extension('framework', ['csrf_protection' => true,]);
  $container->extension('framework', ['csrf_protection' => null,]);
  $container->extension('something_else', ['csrf_protection' => false,]);

  $csrfOption = false;
//^^^^^^^^^^^^^^^^^^^> {{Setting variable to false.}}
  $container->extension('framework', ['csrf_protection' => $csrfOption,]);// Noncompliant {{Make sure disabling CSRF protection is safe here.}}
//                                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


};
