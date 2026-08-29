import pathlib
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[1]
HOOK = ROOT / ".githooks" / "pre-commit"
INSTALLER = ROOT / ".githooks" / "install.sh"


class LocalCiUserBoundaryTest(unittest.TestCase):
    def test_container_runs_as_the_host_user(self):
        source = HOOK.read_text(encoding="utf-8")

        self.assertIn('host_uid="$(id -u)"', source)
        self.assertIn('host_gid="$(id -g)"', source)
        self.assertIn('--user "$host_uid:$host_gid"', source)

    def test_shared_gradle_home_is_rejected_when_ownership_is_mixed(self):
        source = HOOK.read_text(encoding="utf-8")

        self.assertIn('find "$gradle_user_home" -xdev ! -uid "$host_uid" -print -quit', source)
        self.assertIn('refusing mixed-ownership Gradle home', source)
        self.assertIn('-e GRADLE_USER_HOME="$gradle_user_home"', source)
        self.assertIn('-v "$gradle_user_home:$gradle_user_home"', source)

    def test_root_cannot_launch_the_hook(self):
        source = HOOK.read_text(encoding="utf-8")

        self.assertIn('[[ "$host_uid" == "0" ]]', source)
        self.assertIn('refusing to run as root', source)

    def test_supported_same_user_gradle_concurrency_is_not_globally_serialized(self):
        source = HOOK.read_text(encoding="utf-8")

        self.assertNotIn(".rin-build.lock", source)
        self.assertNotIn("flock ", source)
        self.assertLess(source.index("foreign_path="), source.index("docker run --rm"))

    def test_installer_uses_the_git_common_directory(self):
        source = INSTALLER.read_text(encoding="utf-8")

        self.assertIn('git rev-parse --git-common-dir', source)
        self.assertIn('install -m 755 "$repo_root/.githooks/pre-commit"', source)
        self.assertIn('git config core.hooksPath "$stable_hooks_dir"', source)


if __name__ == "__main__":
    unittest.main()
