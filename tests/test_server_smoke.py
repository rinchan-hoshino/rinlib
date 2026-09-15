import importlib.util
from pathlib import Path
import tempfile
import unittest

SPEC = importlib.util.spec_from_file_location('server_smoke', Path(__file__).resolve().parents[1] / 'scripts/server-smoke.py')
SMOKE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(SMOKE)


class ServerSmokeTest(unittest.TestCase):
    def run_fixture(self, body):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            wrapper = root / 'gradlew'
            wrapper.write_text('#!/usr/bin/env python3\n' + body)
            wrapper.chmod(0o755)
            original = SMOKE.ROOT
            try:
                SMOKE.ROOT = root
                SMOKE.run_server(':test:server', 3, root / 'logs')
            finally:
                SMOKE.ROOT = original

    def test_eula_notice_is_not_server_readiness(self):
        with self.assertRaises(RuntimeError):
            self.run_fixture('print("You need to agree to the EULA")\n')

    def test_starting_notice_is_not_server_readiness(self):
        with self.assertRaises(RuntimeError):
            self.run_fixture('print("Starting minecraft server version 1.21.1")\n')

    def test_ready_server_receives_stop_and_exits_cleanly(self):
        self.run_fixture('print(\'Done (0.123s)! For help, type "help"\', flush=True)\nassert input() == "stop"\n')

    def test_crash_after_readiness_is_failure(self):
        with self.assertRaises(RuntimeError):
            self.run_fixture('import sys\nprint(\'Done (0.123s)! For help, type "help"\', flush=True)\ninput()\nsys.exit(1)\n')


if __name__ == '__main__':
    unittest.main()
