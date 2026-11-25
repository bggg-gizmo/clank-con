#!/usr/bin/env bash
set -euo pipefail
A=$(cd "$(dirname "$0")" && pwd); O=$(mktemp -d); trap 'rm -rf "$O"' EXIT
S="$A/src/group/backgroundgremlin/clankcon"
"${JAVA_HOME:?Set JAVA_HOME to supplied JDK}/bin/javac" -encoding UTF-8 --release 8 -d "$O" "$S/Framed.java" "$S/GraphArrayReader.java" "$S/Transcript.java" "$A/tests/group/backgroundgremlin/clankcon/HostTests.java" "$A/tests/group/backgroundgremlin/clankcon/TranscriptTests.java"
"$JAVA_HOME/bin/java" -cp "$O" group.backgroundgremlin.clankcon.HostTests
"$JAVA_HOME/bin/java" -cp "$O" group.backgroundgremlin.clankcon.TranscriptTests
