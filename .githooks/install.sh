#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
git_common_dir="$(git rev-parse --git-common-dir)"
if [[ "$git_common_dir" != /* ]]; then
  git_common_dir="$repo_root/$git_common_dir"
fi
stable_hooks_dir="$git_common_dir/rin-hooks"

install -d -m 700 "$stable_hooks_dir"
install -m 755 "$repo_root/.githooks/pre-commit" "$stable_hooks_dir/pre-commit"
git config core.hooksPath "$stable_hooks_dir"

printf 'RinLib hooks installed at %s\n' "$stable_hooks_dir"
