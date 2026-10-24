#!/bin/bash
# Packs zwyka_Linux.tar.gz (the Compose distributable with a bundled JRE) into an AppImage.
#
# Usage: sh build.sh -v <version> [-t <path to zwyka_Linux.tar.gz>]
set -e

tarball=zwyka_Linux.tar.gz
while getopts v:t: flag
do
    case "${flag}" in
        v) version=${OPTARG};;
        t) tarball=${OPTARG};;
    esac
done

rm -rf AppDir
mkdir -p AppDir/usr/lib/zwyka AppDir/usr/bin \
    AppDir/usr/share/applications AppDir/usr/share/icons/hicolor/scalable/apps
tar -xzf "$tarball" -C AppDir/usr/lib/zwyka
ln -s ../lib/zwyka/bin/zwyka AppDir/usr/bin/zwyka

mv AppDir/usr/lib/zwyka/com.tercad.zwyka.desktop AppDir/usr/share/applications/
mv AppDir/usr/lib/zwyka/com.tercad.zwyka.svg AppDir/usr/share/icons/hicolor/scalable/apps/
# appimagetool expects the desktop entry and the icon in the root as well.
ln -s usr/share/applications/com.tercad.zwyka.desktop AppDir/com.tercad.zwyka.desktop
ln -s usr/share/icons/hicolor/scalable/apps/com.tercad.zwyka.svg AppDir/com.tercad.zwyka.svg
ln -s com.tercad.zwyka.svg AppDir/.DirIcon

cat > AppDir/AppRun <<'EOF'
#!/bin/sh
HERE="$(dirname "$(readlink -f "$0")")"
exec "$HERE/usr/lib/zwyka/bin/zwyka" "$@"
EOF
chmod +x AppDir/AppRun

if [ ! -x appimagetool ]; then
    wget -q -O appimagetool \
        https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage
    chmod +x appimagetool
fi
ARCH=x86_64 ./appimagetool --comp zstd AppDir "Zwyka-${version}-x86_64.AppImage"
