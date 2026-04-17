#!/bin/sh
set -eu

REQUIRED_PLATFORM="platforms;android-36"
REQUIRED_BUILD_TOOLS="build-tools;36.0.0"

has_required_packages() {
    sdk_root="$1"
    [ -n "$sdk_root" ] || return 1
    [ -d "$sdk_root" ] || return 1
    [ -d "$sdk_root/platforms/android-36" ] || return 1
    [ -d "$sdk_root/build-tools/36.0.0" ] || return 1
    return 0
}

if [ -n "${ANDROID_SDK_ROOT:-}" ] && has_required_packages "$ANDROID_SDK_ROOT"; then
    printf '%s\n' "$ANDROID_SDK_ROOT"
    exit 0
fi

if [ -n "${ANDROID_HOME:-}" ] && has_required_packages "$ANDROID_HOME"; then
    printf '%s\n' "$ANDROID_HOME"
    exit 0
fi

SDK_ROOT="$1"
mkdir -p "$SDK_ROOT"

SDKMANAGER="$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"
if [ ! -x "$SDKMANAGER" ]; then
    mkdir -p "$SDK_ROOT/cmdline-tools"
    TMP_ZIP="$SDK_ROOT/cmdline-tools.zip"
    URLS="https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip"

    downloaded=false
    for url in $URLS; do
        if command -v curl >/dev/null 2>&1 && curl -fsSL "$url" -o "$TMP_ZIP"; then
            downloaded=true
            break
        fi
        if command -v wget >/dev/null 2>&1 && wget -q "$url" -O "$TMP_ZIP"; then
            downloaded=true
            break
        fi
    done

    if [ "$downloaded" != true ]; then
        echo "Failed to download Android command line tools." >&2
        exit 1
    fi

    rm -rf "$SDK_ROOT/cmdline-tools/latest"
    unzip -q -o "$TMP_ZIP" -d "$SDK_ROOT/cmdline-tools"
    rm -f "$TMP_ZIP"

    if [ -d "$SDK_ROOT/cmdline-tools/cmdline-tools" ]; then
        mv "$SDK_ROOT/cmdline-tools/cmdline-tools" "$SDK_ROOT/cmdline-tools/latest"
    fi
fi

SDKMANAGER="$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"

if [ ! -x "$SDKMANAGER" ]; then
    echo "sdkmanager not found after installing command line tools." >&2
    exit 1
fi

yes | "$SDKMANAGER" --sdk_root="$SDK_ROOT" --licenses >/dev/null || true
"$SDKMANAGER" --sdk_root="$SDK_ROOT" "$REQUIRED_PLATFORM" "$REQUIRED_BUILD_TOOLS" "platform-tools"

printf '%s\n' "$SDK_ROOT"
