import hashlib, importlib.util, json, struct, tempfile, unittest
from pathlib import Path

SPEC = importlib.util.spec_from_file_location("builder", Path(__file__).with_name("build_xfearth_source.py"))
builder = importlib.util.module_from_spec(SPEC); SPEC.loader.exec_module(builder)

def png_header(width, height):
    return b"\x89PNG\r\n\x1a\n" + struct.pack(">I", 13) + b"IHDR" + struct.pack(">II", width, height)

class BuilderTests(unittest.TestCase):
    def test_manifest_and_hashes(self):
        with tempfile.TemporaryDirectory() as folder:
            root=Path(folder); images=root/"images"; images.mkdir(); out=root/"out"
            payload=png_header(builder.WIDTH,builder.HEIGHT)
            for name in builder.FILES: (images/name).write_bytes(payload)
            builder.build(images,out); manifest=json.loads((out/"xenoearth-source.json").read_text())
            self.assertEqual(manifest["profile"],"earth2000");self.assertEqual(manifest["width"],21504)
            self.assertEqual(manifest["rasters"][builder.FILES[0]],hashlib.sha256(payload).hexdigest())
    def test_missing_input_is_named(self):
        with tempfile.TemporaryDirectory() as folder:
            with self.assertRaisesRegex(ValueError,"HeightMap20k.png"): builder.build(Path(folder),Path(folder)/"out")

if __name__ == "__main__": unittest.main()
