<?php

namespace Illuminate\Database\Eloquent;

class Model
{
}

namespace Lorisleiva\Actions\Concerns;

trait AsAction
{
}

trait AsController
{
}

namespace App\Models;

class Account extends \Illuminate\Database\Eloquent\Model
{
}

class SearchCriteria
{
}

namespace App\Handlers;

use App\Models\Account;
use App\Models\SearchCriteria;
use Lorisleiva\Actions\Concerns\AsAction;
use Lorisleiva\Actions\Concerns\AsController as ControllerAction;

class ShowAccount
{
    use AsAction;

    // Laravel Actions uses asController when the action declares it.
    public function asController(Account $account) // Compliant
    {
    }

    // handle is not the routed entry point when asController is declared.
    public function handle(Account $account) // Noncompliant
    {
    }

    public function helper(Account $account) // Noncompliant
    {
    }
}

class DestroyAccount
{
    use ControllerAction;

    // Laravel Actions falls back to handle when asController is absent.
    public function handle(Account $account) // Compliant
    {
    }
}

class SearchAccounts
{
    use AsAction;

    // Only model-like parameters are suppressed; SearchCriteria is not an Eloquent model or UrlRoutable.
    public function asController(SearchCriteria $criteria) // Noncompliant
    {
    }
}

class StaticAccountAction
{
    use AsAction;

    // asController must be a non-static method for it to be considered
    public static function asController(Account $account) // Noncompliant
    {
    }
}

class ActionLookalike
{
    // The distinctive asController name is sufficient when the parameter is route-bindable.
    public function asController(Account $account) // Compliant
    {
    }
}

namespace App\Actions;

use App\Models\Account;

class ConventionBasedHandler
{
    // Since the class is in a namespace which contains "Action", `handle` is likely a Laravel Actions entry point.
    // Therefore, its model parameter is suppressed.
    public function handle(Account $account) // Compliant
    {
    }
}

namespace App\Services;

use App\Models\Account;

class AccountHandler
{
    // The class has no Laravel Actions trait nor is it define in a namespace that contains "action".
    // `handle` is not likely an entry point. Therefore, its model parameter is reported.
    public function handle(Account $account) // Noncompliant
    {
    }
}

class AccountEndpointAdapter
{
    // asController is distinctive enough without an action-shaped class hierarchy.
    public function asController(Account $account) // Compliant
    {
    }
}

namespace App\Actions;

use Lorisleiva\Actions\Concerns\AsAction;

abstract class BaseAction
{
    use AsAction;
}

namespace App\Workflows;

use App\Actions\BaseAction;
use App\Models\Account;

class InheritedAccountHandler extends BaseAction
{
    // BaseAction is defined in an Actions namespace, so its subclass is likely a Laravel Actions class.
    // Therefore, its model parameter is suppressed.
    public function handle(Account $account) // Compliant
    {
    }
}
