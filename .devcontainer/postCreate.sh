#!/usr/bin/env bash
set -euo pipefail

echo "=== Toolchain ==="
java -version
mvn -version
git --version

DEVSTREAM_URL="https://github.com/simlytics-cloud/devs-streaming.git"
DEVSTREAM_DIR="$HOME/workspaces/devs-streaming"
DEVSTREAM_BRANCH="main"

echo "=== Clone/update devs-streaming (${DEVSTREAM_BRANCH}) ==="
mkdir -p "$HOME/workspaces"
if [ ! -d "${DEVSTREAM_DIR}/.git" ]; then
  git clone "${DEVSTREAM_URL}" "${DEVSTREAM_DIR}"
fi

pushd "${DEVSTREAM_DIR}" >/dev/null
git fetch --all --prune
git checkout "${DEVSTREAM_BRANCH}"
git pull --ff-only || true

echo "=== Build/install devs-streaming into local Maven repo ==="
mvn -DskipTests install
popd >/dev/null

echo "=== Build tutorial repo (irp-java) ==="
# Codespaces opens with the repo as the working directory
mvn -DskipTests verify

echo "=== Done ==="
