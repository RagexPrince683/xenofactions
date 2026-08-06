#!/usr/bin/env python3
"""Build a deterministic, ZIP-compatible Xenofactions Earth map pack."""
import argparse, hashlib, json, re, shutil, sys, tempfile, zipfile
from pathlib import Path, PurePosixPath

FORBIDDEN_DIRS = {"playerdata", "players", "stats", "advancements", "crash-reports", "logs"}
FORBIDDEN_FILES = {"session.lock", "level.dat_old", "uid.dat"}
ALLOWED_TOP = {"region", "data", "DIM-1", "DIM1"}
EPOCH = (1980, 1, 1, 0, 0, 0)

def sanitize_path(value):
    if not value or "\0" in value or len(value) > 512:
        raise ValueError("invalid archive path")
    value = value.replace("\\", "/")
    if value.startswith("/") or value.startswith("//") or re.match(r"^[A-Za-z]:", value):
        raise ValueError("absolute archive path")
    parts = []
    for part in value.split("/"):
        if part in ("", "."): continue
        if part == "..": raise ValueError("archive traversal")
        parts.append(part)
    path = "/".join(parts)
    if not path: raise ValueError("empty archive path")
    return path

def canonical_json(value):
    return json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(",", ":")) + "\n"

def sha256(path):
    h = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""): h.update(block)
    return h.hexdigest()

def collect(source, profile_path=None):
    result = [(source / "level.dat", "template-level.dat"), (profile_path or source / "xenoearth-profile.json", "xenoearth-profile.json")]
    for top in sorted(ALLOWED_TOP):
        root = source / top
        if not root.exists(): continue
        for item in sorted((p for p in root.rglob("*") if p.is_file()), key=lambda p: p.as_posix()):
            relative = item.relative_to(source).as_posix(); low = relative.lower(); first = low.split("/")[0]
            if first in FORBIDDEN_DIRS or item.name.lower() in FORBIDDEN_FILES: continue
            result.append((item, sanitize_path(relative)))
    return sorted(result, key=lambda item: item[1])

def build_manifest(args, profile, files):
    entries = [{"path": arc, "size": src.stat().st_size, "sha256": sha256(src)} for src, arc in files]
    return {"formatVersion": 1, "id": args.id, "displayName": args.display_name, "version": args.version,
            "minecraftVersion": "1.7.10", "effectiveScale": args.effective_scale,
            "width": profile["width"], "height": profile["height"], "populationMode": args.population_mode,
            "bundled": args.bundled, "requiresTemplateLevelDat": True,
            "requiredMods": sorted(set(args.required_mod)), "installedSize": sum(x["size"] for x in entries), "files": entries}

def write_entry(zf, name, data, compression=zipfile.ZIP_DEFLATED):
    info = zipfile.ZipInfo(name, EPOCH); info.compress_type = compression; info.external_attr = 0o100644 << 16
    zf.writestr(info, data, compresslevel=1 if compression == zipfile.ZIP_DEFLATED else None)

def validate_archive(path):
    with zipfile.ZipFile(path) as zf:
        names = [sanitize_path(i.filename) for i in zf.infolist()]
        if len(names) != len(set(names)): raise ValueError("duplicate normalized archive path")
        manifest = json.loads(zf.read("earth-map-pack.json"))
        for entry in manifest["files"]:
            data = zf.read(entry["path"])
            if len(data) != entry["size"] or hashlib.sha256(data).hexdigest() != entry["sha256"]:
                raise ValueError("archive verification failed: " + entry["path"])
        return manifest

def main(argv=None):
    p = argparse.ArgumentParser(); p.add_argument("--input", required=True, type=Path); p.add_argument("--output", required=True, type=Path)
    p.add_argument("--profile", type=Path); p.add_argument("--id", required=True); p.add_argument("--display-name", required=True)
    p.add_argument("--population-mode", required=True, choices=("clean-pregenerated", "already-populated")); p.add_argument("--effective-scale", required=True, type=int)
    p.add_argument("--required-mod", action="append", default=[]); p.add_argument("--version", default="1"); p.add_argument("--bundled", action="store_true"); p.add_argument("--force", action="store_true"); args=p.parse_args(argv)
    if not re.fullmatch(r"[a-z0-9][a-z0-9._-]{2,63}", args.id): p.error("invalid pack ID")
    if args.output.exists() and not args.force: p.error("output exists; pass --force")
    source=args.input.resolve(); profile_path=(args.profile or source/"xenoearth-profile.json").resolve()
    if not (source/"level.dat").is_file(): p.error("input must contain level.dat")
    if not (source/"region").is_dir(): p.error("input must contain region/")
    if not profile_path.is_file(): p.error("xenoearth-profile.json is required (or pass --profile)")
    profile=json.loads(profile_path.read_text(encoding="utf-8"))
    if profile.get("formatVersion") != 1 or profile.get("targetMinecraftVersion") != "1.7.10": p.error("profile must be format 1 for Minecraft 1.7.10")
    if profile.get("effectiveScale") != args.effective_scale: p.error("profile scale disagrees with --effective-scale")
    files=collect(source, profile_path)
    manifest=build_manifest(args,profile,files); args.output.parent.mkdir(parents=True,exist_ok=True)
    temp=args.output.with_suffix(args.output.suffix+".tmp")
    try:
        with zipfile.ZipFile(temp,"w",allowZip64=True) as zf:
            write_entry(zf,"earth-map-pack.json",canonical_json(manifest).encode("utf-8"))
            for src,name in files:
                compression=zipfile.ZIP_STORED if name.endswith(".mca") or name=="template-level.dat" else zipfile.ZIP_DEFLATED
                write_entry(zf,name,src.read_bytes(),compression)
        validate_archive(temp); temp.replace(args.output)
    finally:
        if temp.exists(): temp.unlink()
    print("Built",args.output); print("Pack:",args.id,args.version); print("Files:",len(files)); print("Installed bytes:",manifest["installedSize"]); print("Archive SHA-256:",sha256(args.output))
    return 0
if __name__ == "__main__": sys.exit(main())
