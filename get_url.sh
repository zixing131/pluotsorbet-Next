#!/bin/sh

class=$(unzip -p "$1" META-INF/MANIFEST.MF |\
 grep -i -E "^MIDlet-[0-9]+" |\
 sed "s/.*,\W*//" | tr -d "\t\n\r" |\
 head -n 1 | tail -n 1)

url="http://localhost:8000/index.html?midletClassName=$class&jars=$1"

echo "$url"
