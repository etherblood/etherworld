#!/bin/bash

GIT=$1
CLIENT=$2

rm -r ${CLIENT}etherworld
unzip ${GIT}jlink/target/Etherworld-*.zip -d ${CLIENT}etherworld/
rsync -rc --delete ${GIT}glue/target/assets ${CLIENT}
rsync -rc --delete ${GIT}glue/target/lib ${CLIENT}
mv ${GIT}releng/background.png /var/www/destrostudios/launcher/images/
mv ${GIT}releng/tile.png /var/www/destrostudios/launcher/images/
curl https://destrostudios.com:8080/apps/8/updateFiles
echo done
