#!/bin/bash

#
# ConnectiveStandalone Remote Installation Script
# This script installs the server when updates are released.
# Only works on AutoRelease enabled repository services.
#
# Also, it needs the autorelease.allow.install file on the server.
#

function prepare() {
	destination "/usr/lib/connective-http"
	buildOutput "build/Installations"
}

function build() {
    chmod +x gradlew
    ./gradlew build installation
}

function install() {
	cp -rfv "$BUILDDIR/libs/." "$DEST/libs"
	cp -rfv "$BUILDDIR/ConnectiveStandalone.jar" "$DEST"
}

function postInstall() {
    log Rebooting HTTP server...
    sudo systemctl restart connective-http
}
