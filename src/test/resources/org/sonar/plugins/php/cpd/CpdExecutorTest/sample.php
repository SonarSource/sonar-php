<?php


class MyPhpClass
{
    protected $fAmount;
    protected $fCurrency;

    public function __construct($var1, $currency)
    {
        $this->fAmount   = $amount;
        $this->fCurrency = $currency;
    }

    public function add(IMoney $m)
    {
        return $m->addMoney($this);
    }

    public function addMoney(Money $m)
    {
        if ($this->currency() == $m->currency()) {
            return new Money($this->amount() + $m->amount(), $this->currency());
        }

        return MoneyBag::create($this, $m);
    }

    public function addMoneyBag(MoneyBag $s)
    {
        return $s->addMoney($this);
    }

    public function amount()
    {
        return $this->fAmount;
    }

    public function currency()
    {
        return $this->fCurrency;
    }

    public function equals($anObject)
    {
        if ($this->isZero() &&
            $anObject instanceof IMoney) {
            return $anObject->isZero();
        }

        if ($anObject instanceof Money) {
            return ($this->currency() == $anObject->currency() &&
                    $this->amount() == $anObject->amount());
        }

        return FALSE;
    }

    public function hashCode()
    {
        return crc32($this->fCurrency) + $this->fAmount;
    }

    public function isZero()
    {
        return $this->amount() == 0;
    }

    public function multiply($factor)
    {
        return new Money($this->amount() * $factor, $this->currency());
    }

    public function negate()
    {
        return new Money(-1 * $this->amount(), $this->currency());
    }

    public function subtract(IMoney $m)
    {
        return $this->add($m->negate());
    }

    public function toString()
    {
        return '[' . $this->amount() . ' ' . $this->currency() . ']';
    }

    public function appendTo(MoneyBag $m)
    {
        $m->appendMoney($this);
    }
}
?>