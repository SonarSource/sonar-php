<?php

namespace Illuminate\Database\Eloquent;

class Model
{
}

namespace Illuminate\Contracts\Routing;

interface UrlRoutable
{
}

namespace Illuminate\Routing;

class Controller
{
}

namespace Illuminate\Http;

class Request
{
}

namespace Illuminate\Foundation\Http;

class FormRequest extends \Illuminate\Http\Request
{
}

namespace App\Domain;

class DomainAccount extends \Illuminate\Database\Eloquent\Model
{
}

class RouteValue implements \Illuminate\Contracts\Routing\UrlRoutable
{
}

namespace App\Http\Requests;

class AccountIndexRequest extends \Illuminate\Foundation\Http\FormRequest
{
}

namespace App\Models;

class SearchCriteria
{
}

class ExportRequest
{
}

namespace App\Http\Controllers\Accounts;

use App\Domain\DomainAccount;
use App\Domain\RouteValue;
use App\Http\Requests\AccountIndexRequest;
use App\Http\Requests\ApproveRequest as Approval;
use App\DTO\Approval as SearchRequest;
use App\Models\Account;
use App\Models\Comment;
use App\Models\Company;
use App\Models\ExportRequest;
use App\Models\Post;
use App\Models\SearchCriteria;
use Illuminate\Http\Request;

class AccountController
{
    public function index(AccountIndexRequest $request, Account $account, Company $company)
    {
        consume($request, $company);
    }

    // Parameter names do not need to match the model class names.
    public function show(Post $article, Comment $discussion)
    {
        consume($discussion);
    }

    // A Request-like parameter identifies custom controller actions.
    public function archive(Request $request, Account $account)
    {
        consume($request);
    }

    // A custom FormRequest subtype also identifies a custom controller action.
    public function approve(AccountIndexRequest $request, Account $account)
    {
        consume($request);
    }

    // Request detection uses the resolved class name rather than its alias.
    public function approveAlias(Approval $request, Account $account)
    {
        consume($request);
    }

    // An unresolved Request-like type supports single-file IDE analysis.
    public function publish(UnknownRequest $request, Account $account)
    {
        consume($request);
    }

    // A declared lookalike with definitive non-Request ancestry is not Request-like.
    public function export(ExportRequest $request, Account $account) // Noncompliant {{Remove the unused function parameter "$account".}}
    {
        consume($request);
    }

    public function filter(SearchRequest $request, Account $account) // Noncompliant {{Remove the unused function parameter "$account".}}
    {
        consume($request);
    }

    // A conventional action name is sufficient without a Request parameter.
    public function destroy(Account $account)
    {
    }

    // Non-conventional methods without a Request-like parameter are still checked.
    public function helper(Account $account) // Noncompliant
    {
    }

    public static function store(Request $request, Account $account) // Noncompliant {{Remove the unused function parameter "$account".}}
    {
        consume($request);
    }

    protected function edit(Account $account) // Noncompliant
    {
    }

    private function update(Account $account) // Noncompliant
    {
    }

    // A class type outside a conventional model namespace remains checked.
    public function create(Request $request, \App\Data\Account $account) // Noncompliant {{Remove the unused function parameter "$account".}}
    {
        consume($request);
    }

    // The namespace fallback must not override definitive non-model type information.
    public function search(Request $request, SearchCriteria $criteria) // Noncompliant {{Remove the unused function parameter "$criteria".}}
    {
        consume($request);
    }
}

class DomainController
{
    // Semantic ancestry recognizes models outside a conventional model namespace.
    public function show(DomainAccount $article)
    {
    }

    public function destroy(RouteValue $value)
    {
    }
}

class AccountHandler
{
    public function show(Account $account) // Noncompliant
    {
    }
}

namespace App\Endpoints;

use App\Models\Account;

class AccountEndpoint extends \Illuminate\Routing\Controller
{
    // Inheritance from Laravel's optional base class establishes the controller role.
    public function show(Account $account)
    {
    }
}

namespace App\Services;

use App\Models\Account;

class AccountController
{
    public function show(Account $account) // Noncompliant
    {
    }
}
