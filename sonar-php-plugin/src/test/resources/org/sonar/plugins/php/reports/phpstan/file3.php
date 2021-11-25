<?php
trait FooTrait
{
	public function doTraitFoo()
	{
		$this->doFoo(); // undefined
	}
}

class Bar
{
	use FooTrait;
}