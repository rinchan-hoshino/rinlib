#!/usr/bin/env bash
set -euo pipefail

python3 -m unittest discover -s tests -p 'test_*.py'
./gradlew --no-daemon build

if [[ "${RUN_SERVER_SMOKE:-1}" == "1" ]]; then
  : "${RINLIB_EULA_FILE:?Set RINLIB_EULA_FILE to an accepted eula.txt, or RUN_SERVER_SMOKE=0 for build-only checks}"
  ./scripts/server-smoke.sh --eula "$RINLIB_EULA_FILE"
else
  echo "server smoke skipped: RUN_SERVER_SMOKE=$RUN_SERVER_SMOKE"
fi
