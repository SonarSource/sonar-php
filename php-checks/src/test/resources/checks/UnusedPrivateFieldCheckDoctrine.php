<?php

namespace DoctrineCases;

use Doctrine\ORM\Mapping as ORM;
use Doctrine\ORM\Mapping\Column as DbColumn;
use Doctrine\ORM\Mapping\ManyToOne as Relation;
use Other\Mapping as OtherMapping;

#[ORM\Entity]
class PersistedEntity {
  #[ORM\Column(generated: 'ALWAYS', insertable: false, updatable: false)]
  private $generatedColumn; // Compliant

  #[DbColumn]
  private $aliasedColumn; // Compliant

  #[Relation]
  private $relationship; // Compliant

  #[\Doctrine\ORM\Mapping\OneToMany]
  private $qualifiedRelationship; // Compliant

  /** @ORM\Id */
  private $annotatedId; // Compliant

  /** @DbColumn */
  private $annotatedAlias; // Compliant

  /** @\Doctrine\ORM\Mapping\JoinColumn */
  private $annotatedJoinColumn; // Compliant

  /** @Doctrine\ORM\Mapping\Column */
  private $annotatedUnimportedQualifiedColumn; // Compliant

  #[OtherMapping\Column]
  private $otherMapping; // Noncompliant

  private $unmapped; // Noncompliant

  private const UNRELATED = 1; // Noncompliant
}

class NotAnEntity {
  /** @ORM\ManyToMany */
  private $mappedWithoutEntityMarker; // Compliant

  #[ORM\JoinTable]
  private $joinTable; // Compliant

  public function __construct(#[ORM\Column] private string $mappedPromoted) { // Compliant
  }

  private $unmapped; // Noncompliant
}

namespace GroupedDoctrineCases;

use Doctrine\ORM\Mapping\{Column as Field, OneToOne};

class GroupedImports {
  #[Field]
  private $column; // Compliant

  /** @OneToOne */
  private $association; // Compliant

  #[\Doctrine\ORM\Mapping\Id]
  private $qualifiedId; // Compliant
}

namespace OtherDoctrineCases;

// There is no Doctrine use statement in this namespace, so #[ORM\Column] is not recognized as a mapping.
// The Doctrine import from the earlier namespace must not leak here; ordinary comments are not annotations.
class NoLeakedAlias {
  #[ORM\Column]
  private $unresolvedAlias; // Noncompliant

  // @\Doctrine\ORM\Mapping\Column
  private $ordinaryComment; // Noncompliant
}

namespace Doctrine\ORM\Mapping;

class MappingInOwnNamespace {
  #[Column]
  private $mappedColumn; // Compliant

  #[namespace\Column]
  private $explicitlyRelativeColumn; // Compliant

  private $unmapped; // Noncompliant
}

namespace Doctrine\ORM;

class MappingInParentNamespace {
  #[Mapping\Column]
  private $mappedColumn; // Compliant
}

namespace ConflictingDoctrineAlias;

use Other\Annotations as Doctrine;

class AliasTakesPrecedence {
  /** @Doctrine\ORM\Mapping\Column */
  private $notAMapping; // Noncompliant
}
