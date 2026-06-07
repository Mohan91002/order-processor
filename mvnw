#!/bin/sh
# Minimal Maven wrapper: downloads Maven if needed, then runs it.
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
PROPS="$DIR/.mvn/wrapper/maven-wrapper.properties"
if [ ! -f "$PROPS" ]; then
    echo "Missing $PROPS" >&2
    exit 1
fi

DIST_URL=$(grep -E '^distributionUrl=' "$PROPS" | cut -d= -f2-)
DIST_FILE=$(basename "$DIST_URL")
DIST_NAME=$(echo "$DIST_FILE" | sed 's/-bin\.zip$//')

CACHE="$HOME/.m2/wrapper/dists/$DIST_NAME"
MVN_HOME="$CACHE/$DIST_NAME"

if [ ! -x "$MVN_HOME/bin/mvn" ]; then
    mkdir -p "$CACHE"
    echo "Downloading Maven from $DIST_URL ..." >&2
    curl -fsSL "$DIST_URL" -o "$CACHE/$DIST_FILE"
    unzip -q -o "$CACHE/$DIST_FILE" -d "$CACHE"
    rm -f "$CACHE/$DIST_FILE"
    chmod +x "$MVN_HOME/bin/mvn"
fi

exec "$MVN_HOME/bin/mvn" "$@"
