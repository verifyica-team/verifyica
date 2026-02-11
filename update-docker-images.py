#!/usr/bin/env python3
import argparse
import re
import sys
import time
from dataclasses import dataclass
from typing import Dict, List, Optional, Tuple, Set

import requests

SEMVER_RE = re.compile(r"^(?P<maj>0|[1-9]\d*)\.(?P<min>0|[1-9]\d*)\.(?P<pat>0|[1-9]\d*)$")


@dataclass(frozen=True)
class ImageRef:
    original: str
    registry: str          # "" => Docker Hub default
    namespace: str         # Docker Hub namespace (default "library" if none)
    repo: str
    tag: str


def parse_image_ref(line: str) -> Optional[ImageRef]:
    s = line.strip()
    if not s or s.startswith("#"):
        return None
    if ":" not in s:
        return None
    if "@" in s:
        return None

    name, tag = s.rsplit(":", 1)
    registry = ""
    path = name

    # Explicit registry? ('.' or ':' in first segment)
    if "/" in name:
        first = name.split("/", 1)[0]
        if "." in first or ":" in first:
            registry = first
            path = name.split("/", 1)[1]

    parts = path.split("/")
    if len(parts) == 1:
        namespace = "library"
        repo = parts[0]
    else:
        namespace = parts[0]
        repo = "/".join(parts[1:])

    return ImageRef(original=s, registry=registry, namespace=namespace, repo=repo, tag=tag)


def semver_tuple(tag: str) -> Optional[Tuple[int, int, int]]:
    m = SEMVER_RE.match(tag)
    if not m:
        return None
    return (int(m.group("maj")), int(m.group("min")), int(m.group("pat")))


def docker_hub_list_tags(namespace: str, repo: str, session: requests.Session) -> List[str]:
    tags: List[str] = []
    url = f"https://hub.docker.com/v2/repositories/{namespace}/{repo}/tags"
    params = {"page_size": 100}
    while url:
        r = session.get(url, params=params, timeout=20)
        if r.status_code == 429:
            time.sleep(2)
            continue
        r.raise_for_status()
        data = r.json()
        for item in data.get("results", []):
            name = item.get("name")
            if isinstance(name, str):
                tags.append(name)
        url = data.get("next")
        params = None
    return tags


def best_patch_for_track(all_tags: List[str], maj: int, minor: int) -> Optional[Tuple[Tuple[int, int, int], str]]:
    best: Optional[Tuple[Tuple[int, int, int], str]] = None
    for t in all_tags:
        tv = semver_tuple(t)
        if tv is None:
            continue
        if tv[0] == maj and tv[1] == minor:
            if best is None or tv > best[0]:
                best = (tv, t)
    return best


def find_tracks(all_tags: List[str]) -> Dict[Tuple[int, int], Tuple[int, int, int]]:
    """
    Return { (maj, minor) -> best semver tuple } for all semver tags.
    """
    tracks: Dict[Tuple[int, int], Tuple[int, int, int]] = {}
    for t in all_tags:
        tv = semver_tuple(t)
        if tv is None:
            continue
        key = (tv[0], tv[1])
        cur = tracks.get(key)
        if cur is None or tv > cur:
            tracks[key] = tv
    return tracks


def format_image_ref(ref: ImageRef, new_tag: str) -> str:
    if ref.registry:
        return f"{ref.registry}/{ref.namespace}/{ref.repo}:{new_tag}"
    if ref.namespace != "library":
        return f"{ref.namespace}/{ref.repo}:{new_tag}"
    return f"{ref.repo}:{new_tag}"


def main() -> int:
    ap = argparse.ArgumentParser(description="Update Docker image tags in a file (Docker Hub).")
    ap.add_argument("path", help="Path to env file (comments with # are preserved).")
    ap.add_argument("--in-place", action="store_true",
                    help="Rewrite the file in-place. If omitted, prints updated content to stdout.")
    ap.add_argument("--dry-run", action="store_true",
                    help="Print changes (to stderr). Does not write when used with --in-place.")
    ap.add_argument("--add-tracks", action="store_true",
                    help="Append newly discovered major.minor tracks (e.g. 0.5.x) to the file.")
    ap.add_argument("--only-newer-tracks", action="store_true",
                    help="Only consider tracks with (maj,minor) greater than the max currently present.")
    args = ap.parse_args()

    with open(args.path, "r", encoding="utf-8") as f:
        lines = f.readlines()

    session = requests.Session()
    session.headers.update({"User-Agent": "image-track-updater/1.0"})

    # Cache tags per (registry, namespace, repo)
    tag_cache: Dict[Tuple[str, str, str], List[str]] = {}

    # First pass: collect existing refs + tracks
    refs: List[Tuple[int, str, ImageRef]] = []  # (line_index, raw_line, parsed_ref)
    existing_tracks: Dict[Tuple[str, str, str], Set[Tuple[int, int]]] = {}
    max_track_per_image: Dict[Tuple[str, str, str], Optional[Tuple[int, int]]] = {}

    for i, line in enumerate(lines):
        ref = parse_image_ref(line)
        if ref is None:
            continue
        if ref.registry:
            # Docker Hub only in this script (consistent with your examples)
            continue

        tv = semver_tuple(ref.tag)
        if tv is None:
            continue

        key = (ref.registry, ref.namespace, ref.repo)
        refs.append((i, line, ref))
        existing_tracks.setdefault(key, set()).add((tv[0], tv[1]))

    for key, tracks in existing_tracks.items():
        max_track_per_image[key] = max(tracks) if tracks else None

    # Fetch tags for each image we saw
    for key in existing_tracks.keys():
        _, namespace, repo = key
        try:
            tag_cache[key] = docker_hub_list_tags(namespace, repo, session)
        except Exception as e:
            print(f"WARN: failed fetching tags for {namespace}/{repo}: {e}", file=sys.stderr)
            tag_cache[key] = []

    changed = False
    out_lines = list(lines)

    # Update existing lines within their tracks
    for i, raw, ref in refs:
        tv = semver_tuple(ref.tag)
        if tv is None:
            continue
        key = (ref.registry, ref.namespace, ref.repo)
        best = best_patch_for_track(tag_cache[key], tv[0], tv[1])
        if best is None:
            continue
        best_tv, best_tag = best
        if best_tv > tv:
            new_ref = format_image_ref(ref, best_tag)
            if args.dry_run:
                print(f"UPDATE  {ref.original}  ->  {new_ref}", file=sys.stderr)

            # preserve indentation + newline
            prefix = raw[: len(raw) - len(raw.lstrip("\t "))]
            suffix = "\r\n" if raw.endswith("\r\n") else "\n" if raw.endswith("\n") else ""
            out_lines[i] = prefix + new_ref + suffix
            changed = True

    # Discover new tracks and optionally append
    appended_any = False
    for key, tracks in existing_tracks.items():
        _, namespace, repo = key
        all_tags = tag_cache.get(key, [])
        all_tracks = find_tracks(all_tags)  # (maj,minor) -> best semver tuple

        existing = tracks
        max_existing = max(existing) if existing else None

        # Candidate tracks are those not already present
        candidates = [t for t in all_tracks.keys() if t not in existing]
        candidates.sort()

        if args.only_newer_tracks and max_existing is not None:
            candidates = [t for t in candidates if t > max_existing]

        # For each candidate, propose latest patch tag (from all_tracks best tuple)
        for (maj, minor) in candidates:
            best_tv = all_tracks[(maj, minor)]
            best_tag = f"{best_tv[0]}.{best_tv[1]}.{best_tv[2]}"
            new_line = f"{namespace}/{repo}:{best_tag}\n"

            if args.dry_run or not args.add_tracks:
                print(f"NEWTRACK {namespace}/{repo}:{maj}.{minor}.x -> suggest {new_line.strip()}",
                      file=sys.stderr)

            if args.add_tracks:
                out_lines.append(new_line)
                appended_any = True
                changed = True

    if args.in_place and changed and not args.dry_run:
        with open(args.path, "w", encoding="utf-8") as f:
            f.writelines(out_lines)
    else:
        sys.stdout.write("".join(out_lines))

    # Exit code: 0 = no change, 2 = changed
    return 2 if changed else 0


if __name__ == "__main__":
    raise SystemExit(main())

