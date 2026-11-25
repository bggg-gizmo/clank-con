#!/usr/bin/env bash
set -euo pipefail
A=$(cd "$(dirname "$0")" && pwd); O=$(mktemp -d); trap 'rm -rf "$O"' EXIT
S="$A/src/group/backgroundgremlin/clankcon"
"${JAVA_HOME:?Set JAVA_HOME to supplied JDK}/bin/javac" -encoding UTF-8 --release 8 -d "$O" "$S/Framed.java" "$S/GraphArrayReader.java" "$A/tests/group/backgroundgremlin/clankcon/HostTests.java"
"$JAVA_HOME/bin/java" -cp "$O" group.backgroundgremlin.clankcon.HostTests
