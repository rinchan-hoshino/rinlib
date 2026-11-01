import pathlib
import subprocess
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
CHECKER = ROOT / 'tools/check_adapter_api.py'


class AdapterApiTest(unittest.TestCase):
    def check(self, *directories):
        return subprocess.run(['python3', str(CHECKER), *map(str, directories)],
                              capture_output=True, text=True)

    def test_identical_types_allow_different_parameter_names_and_bodies(self):
        with tempfile.TemporaryDirectory() as temporary:
            a, b = [pathlib.Path(temporary) / name for name in ['a', 'b']]
            for directory, parameter, body in [(a, 'id', '1'), (b, 'name', '2')]:
                directory.mkdir()
                (directory / 'Example.java').write_text(
                    'package dev.rinchan.rinlib.api; public final class Example {\n'
                    f' public static int value(String {parameter}) {{ return {body}; }}\n}}')
            result = self.check(a, b)
            self.assertEqual(0, result.returncode, result.stderr)

    def test_changed_import_or_missing_class_is_rejected(self):
        with tempfile.TemporaryDirectory() as temporary:
            a, b = [pathlib.Path(temporary) / name for name in ['a', 'b']]
            for directory, imported in [(a, 'java.util.Date'), (b, 'java.sql.Date')]:
                directory.mkdir()
                (directory / 'Example.java').write_text(
                    f'package dev.rinchan.rinlib.api; import {imported};\n'
                    'public final class Example {\n public static Date value() { return null; }\n}')
            self.assertNotEqual(0, self.check(a, b).returncode)
            (b / 'Example.java').unlink()
            self.assertNotEqual(0, self.check(a, b).returncode)

    def test_version_specific_identifier_is_rejected_even_when_all_adapters_agree(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = pathlib.Path(temporary)
            (directory / 'Example.java').write_text(
                'package dev.rinchan.rinlib.api; import net.minecraft.resources.ResourceLocation;\n'
                'public final class Example {\n public static void value(ResourceLocation id) {}\n}')
            self.assertNotEqual(0, self.check(directory).returncode)

    def test_an_adapter_moved_out_of_the_api_package_is_rejected(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = pathlib.Path(temporary)
            (directory / 'Example.java').write_text(
                'package dev.rinchan.rinlib.minecraft; public final class Example {}')
            self.assertNotEqual(0, self.check(directory).returncode)

    def test_configured_stable_apis_agree(self):
        result = self.check()
        self.assertEqual(0, result.returncode, result.stderr)

    def test_data_component_era_targets_share_the_same_public_contract(self):
        versions = ['1.20.6', '1.21', '1.21.1', '1.21.2', '1.21.3', '1.21.4',
                    '1.21.5', '1.21.6', '1.21.7', '1.21.8', '1.21.9', '1.21.10',
                    '1.21.11', '26.1', '26.1.1', '26.1.2', '26.2']
        result = self.check(*(ROOT / 'adapters' / version / 'dev/rinchan/rinlib/api'
                              for version in versions))
        self.assertEqual(0, result.returncode, result.stderr)


if __name__ == '__main__':
    unittest.main()
