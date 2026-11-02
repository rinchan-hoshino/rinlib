import json
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]


class ProfileMetadataTest(unittest.TestCase):
    def generate(self, minecraft, loader, directory):
        subprocess.run(['python3', str(ROOT / 'tools/profile_metadata.py'), minecraft,
                        loader, '--output', str(directory)], check=True, capture_output=True)

    def test_native_fabric_metadata_references_only_present_mixins(self):
        with tempfile.TemporaryDirectory() as temporary:
            output = Path(temporary)
            self.generate('1.21.1', 'fabric', output)
            metadata = json.loads((output / 'fabric.mod.json').read_text())
            self.assertEqual(['rinlib.commands.mixins.json', 'rinlib.structure.mixins.json', 'rinlib.mixins.json'], metadata['mixins'])
            self.assertEqual(['1.21.1'], metadata['depends']['minecraft'])
            for name in metadata['mixins']:
                self.assertTrue((output / name).is_file())

    def test_former_portable_is_a_native_mod_without_absent_mixins(self):
        with tempfile.TemporaryDirectory() as temporary:
            output = Path(temporary)
            self.generate('1.20.4', 'neoforge', output)
            text = (output / 'META-INF/mods.toml').read_text()
            self.assertIn('modLoader="javafml"', text)
            self.assertNotIn('[[mixins]]', text)
            self.assertIn('versionRange="[1.20.4]"', text)

    def test_snapshot_uses_fabric_normalized_version(self):
        with tempfile.TemporaryDirectory() as temporary:
            output = Path(temporary)
            self.generate('22w24a', 'fabric', output)
            metadata = json.loads((output / 'fabric.mod.json').read_text())
            self.assertEqual(['1.19.1-alpha.22.24.a'], metadata['depends']['minecraft'])


if __name__ == '__main__':
    unittest.main()
