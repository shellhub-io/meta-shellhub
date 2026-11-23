#!/bin/sh
# Run oelint-adv over meta-shellhub. Requires oelint-adv on PATH.
#
# Configuration is declarative and lives at the layer root: .oelint.cfg
# (--release plus the bbclassextend suppression) and oelint.constants.json
# (the layer constant-DB additions). Both are auto-loaded, so this script only
# enumerates the files to lint.
set -eu

unset CDPATH

here=$(cd -- "$(dirname -- "$0")" && pwd)
layer=$(cd -- "$here/../.." && pwd)
cd -- "$layer"

files=$(find . \
    \( -name '*.bb' -o -name '*.bbappend' -o -name '*.bbclass' -o -name '*.inc' \) \
    | sort)

# Run serially: parallel workers race while loading the layer constants and
# emit spurious "unknown variable/override" findings. Pass '--jobs N' to override.
# shellcheck disable=SC2086
exec oelint-adv --jobs 1 "$@" $files
