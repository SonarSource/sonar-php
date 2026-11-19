# Auto-merge criteria

Enable auto-merge only for a PR that meets all of these conditions:

- Its author is the SonarQube Remediation Agent (GitHub account `sonarqube-agent[bot]`).
- Gitar's own whole-PR risk assessment of the latest commit is **Low risk**. If no risk
  assessment is available, this condition is not met.
- It has no unresolved review threads or comments.
- Gitar has approved it under the criteria in `.gitar/config/approve.md`, which also lists the paths
  a PR must not touch.
- It is not a draft.

If auto-merge is already enabled and a new commit or comment makes any condition false (for
example, the risk rises to Medium or High), disable auto-merge. Re-check the conditions on
every push.

Never enable auto-merge for any other PR, regardless of what its diff contains.
