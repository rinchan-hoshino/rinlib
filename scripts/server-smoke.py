#!/usr/bin/env python3
"""Start each configured dedicated server, then stop it cleanly after readiness."""
import argparse
import os
from pathlib import Path
import queue
import re
import signal
import shutil
import subprocess
import threading
import time

ROOT = Path(__file__).resolve().parents[1]
READY = re.compile(r'Done \([0-9.,]+s\)! For help, type "help"')


def run_server(task, timeout, output):
    output.mkdir(parents=True, exist_ok=True)
    log_path = output / (task.strip(':').replace(':', '-') + '.log')
    lines = queue.Queue()
    process = subprocess.Popen(
        [str(ROOT / 'gradlew'), '--no-daemon', '--console=plain', task],
        cwd=ROOT, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT, text=True, start_new_session=True)

    def read_output():
        with log_path.open('w') as log:
            for line in process.stdout:
                log.write(line)
                log.flush()
                lines.put(line)
        lines.put(None)

    reader = threading.Thread(target=read_output, daemon=True)
    reader.start()
    ready = False
    deadline = time.monotonic() + timeout
    try:
        while time.monotonic() < deadline:
            try:
                line = lines.get(timeout=min(1, max(0.01, deadline - time.monotonic())))
            except queue.Empty:
                continue
            if line is None:
                break
            if not ready and READY.search(line):
                ready = True
                process.stdin.write('stop\n')
                process.stdin.flush()
        code = process.wait(timeout=max(1, deadline - time.monotonic()))
        if not ready or code != 0:
            raise RuntimeError(f'{task}: server did not start and stop successfully; see {log_path}')
    finally:
        if process.poll() is None:
            os.killpg(process.pid, signal.SIGTERM)
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                os.killpg(process.pid, signal.SIGKILL)
                process.wait()
        reader.join(timeout=10)
        process.stdin.close()
        process.stdout.close()
    print(f'{task}: ready and cleanly stopped ({log_path})')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('tasks', nargs='*', default=[':fabric:runServer', ':neoforge:runServer'])
    parser.add_argument('--eula', type=Path, help='Copy an existing accepted eula.txt into the selected run directories')
    parser.add_argument('--timeout', type=int, default=600)
    parser.add_argument('--output', type=Path, default=ROOT / 'build/server-smoke')
    args = parser.parse_args()
    if args.timeout <= 0:
        parser.error('--timeout must be positive')
    if args.eula and 'eula=true' not in args.eula.read_text():
        parser.error('An accepted eula.txt is required')
    for task in args.tasks:
        if args.eula:
            module = task.strip(':').split(':')[0]
            if module not in {'fabric', 'neoforge'}:
                parser.error('Automatic EULA setup supports the fabric and neoforge run directories')
            directory = ROOT / module / 'run'
            directory.mkdir(parents=True, exist_ok=True)
            if args.eula.resolve() != (directory / 'eula.txt').resolve():
                shutil.copyfile(args.eula, directory / 'eula.txt')
        run_server(task, args.timeout, args.output)


if __name__ == '__main__':
    main()
