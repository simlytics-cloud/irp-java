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

echo "=== Install devs-streaming to local Maven repo (~/.m2) ==="
mvn -DskipTests install
popd >/dev/null

echo "=== Build irp-java ==="
# Codespaces opens in /workspaces/irp-java
mvn -DskipTests verify

echo "=== Done ==="
