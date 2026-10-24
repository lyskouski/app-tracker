#!/bin/bash

while getopts v:s: flag
do
    case "${flag}" in
        v) version=${OPTARG};;
        s) hash=${OPTARG};;
    esac
done

date=$(date '+%Y-%m-%d')

sed -i s/{VERSION}/$version/g com.tercad.zwyka.yml
sed -i s/{SHA256}/$hash/g com.tercad.zwyka.yml

sed -i s/{VERSION}/$version/g com.tercad.zwyka.metainfo.xml
sed -i s/{DATE}/$date/g com.tercad.zwyka.metainfo.xml
