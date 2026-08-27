#!/usr/bin/env python3
"""Build the external earth2000 raster package consumed by XenoFactions."""
import argparse, hashlib, json, shutil, struct
from pathlib import Path

FILES = ("HeightMap20k.png", "BiomeMap20k.png", "WaterMap20k.png", "Ice20k.png", "globecover20k.png")
WIDTH, HEIGHT = 21504, 10752

def dimensions(path):
    with path.open("rb") as stream:
        header = stream.read(24)
    if len(header) != 24 or header[:8] != b"\x89PNG\r\n\x1a\n" or header[12:16] != b"IHDR":
        raise ValueError("{} is not a PNG".format(path))
    return struct.unpack(">II", header[16:24])

def digest(path):
    result = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            result.update(block)
    return result.hexdigest()

def build(images, output):
    if output.exists() and any(output.iterdir()):
        raise ValueError("output must be absent or empty: {}".format(output))
    sources = []
    for name in FILES:
        path = images / name
        if not path.is_file():
            raise ValueError("missing required source raster: {}".format(path))
        actual = dimensions(path)
        if actual != (WIDTH, HEIGHT):
            raise ValueError("{} must be {}x{}, got {}x{}".format(name, WIDTH, HEIGHT, *actual))
        sources.append((name, path, digest(path)))
    output.mkdir(parents=True, exist_ok=True)
    for name, path, _ in sources:
        shutil.copy2(str(path), str(output / name))
    manifest = {
        "formatVersion": 1, "sourceId": "earth2000", "profile": "earth2000",
        "minecraftVersion": "1.7.10", "effectiveScale": 2000,
        "width": WIDTH, "height": HEIGHT, "minimumX": -10752,
        "maximumX": 10751, "minimumZ": -5376, "maximumZ": 5375,
        "seaLevel": 62, "generationMode": "RASTER_PREGEN", "generatorVersion": "1",
        "rasters": {name: sha for name, _, sha in sources},
    }
    (output / "xenoearth-source.json").write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--profile", choices=("earth2000",), required=True)
    parser.add_argument("--images", type=Path, default=Path("images"))
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        build(args.images, args.output)
    except ValueError as error:
        parser.error(str(error))

if __name__ == "__main__":
    main()
