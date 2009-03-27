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

}
?>