#!/bin/bash
# Pins the manifest to a released tag: sh patch.sh -v <version> -c <commit of the tag>

while getopts v:c: flag
do
    case "${flag}" in
        v) version=${OPTARG};;
        c) commit=${OPTARG};;
    esac
done

sed -i s/{VERSION}/$version/g com.tercad.zwyka.yml
sed -i s/{COMMIT}/$commit/g com.tercad.zwyka.yml
