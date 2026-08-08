import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import validate_xf_command_localization as validation


class XFCommandLocalizationTest(unittest.TestCase):
    def test_every_catalog_command_has_english_name_and_description(self):
        command_ids = validation.validate()
        self.assertTrue(command_ids)


if __name__ == "__main__":
    unittest.main()
