#!/bin/bash

GIT=$1
CLIENT=$2

rm -r ${CLIENT}*
unzip ${GIT}jlink/target/Etherworld-*.zip -d ${CLIENT}
rsync -rc --delete ${GIT}glue/target/assets ${CLIENT}
rsync -rc --delete ${GIT}glue/target/libs ${CLIENT}
curl https://destrostudios.com:8080/apps/8/updateFiles
echo updated files
