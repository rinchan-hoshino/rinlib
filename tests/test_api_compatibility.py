import unittest
from verify_artifacts import verify_existing_api

BASELINE = ['public final class example.Existing {', 'public static int value();', 'descriptor: ()I', '}']

class ApiCompatibilityTest(unittest.TestCase):
    def test_new_class_can_be_added_without_changing_existing_binary_api(self):
        verify_existing_api(BASELINE + ['public interface example.Addition {', '}'], BASELINE)

    def test_removed_member_is_rejected(self):
        with self.assertRaises(AssertionError):
            verify_existing_api(['public final class example.Existing {', '}'], BASELINE)

    def test_changed_member_descriptor_is_rejected(self):
        with self.assertRaises(AssertionError):
            verify_existing_api([line.replace('()I', '()J') for line in BASELINE], BASELINE)
