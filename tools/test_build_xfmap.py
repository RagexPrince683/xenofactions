import json, tempfile, unittest, zipfile
from pathlib import Path
import build_xfmap as tool
class BuilderTests(unittest.TestCase):
 def test_sanitizer_rejects_traversal_and_absolute_paths(self):
  for value in ("../x", "/x", "C:\\x", "\\\\server\\x", "a/../../x", "a\0b"):
   with self.assertRaises(ValueError): tool.sanitize_path(value)
 def test_manifest_json_is_deterministic(self):
  value={"z":1,"a":["é",2]};self.assertEqual(tool.canonical_json(value),tool.canonical_json(value));self.assertTrue(tool.canonical_json(value).startswith('{"a"'))
 def test_duplicate_normalized_paths_rejected(self):
  with tempfile.TemporaryDirectory() as d:
   path=Path(d)/"x.xfmap"
   with zipfile.ZipFile(path,"w") as z: z.writestr("a/./b",b"1");z.writestr("a/b",b"2")
   with self.assertRaises(ValueError): tool.validate_archive(path)
if __name__ == "__main__": unittest.main()
