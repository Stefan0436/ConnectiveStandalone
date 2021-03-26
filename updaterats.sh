#!/bin/bash
git="https://aerialworks.ddns.net/ASF/RATS.git"
dir="$(pwd)"

echo 'Updating RaTs! installation for libraries...'
rm -rf libraries

echo Cloning git repository...
tmpdir="/tmp/build-rats-connective-http-standalone/$(date "+%s-%N")"
rm -rf "$tmpdir"
mkdir -p "$tmpdir"
git clone $git "$tmpdir"
cd "$tmpdir"
echo

function exitmeth() {
    cd "$dir"
    rm -rf "$tmpdir"
    echo
    exit $1
}

function execute() {
    gradle installation || return $?
    mkdir "$dir/libraries"
    cp -r "build/Installations/"*.jar "$dir/libraries"
}

echo Building...
execute
exitmeth $?
