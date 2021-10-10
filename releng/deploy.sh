#!/bin/bash

GIT=$1
CLIENT=$2

rm -r ${CLIENT}/java
unzip ${GIT}/jlink/target/jlink-*.zip -d ${CLIENT}/java/
rsync -rc --delete ${GIT}/glue/target/assets ${CLIENT}/
rsync -rc --delete ${GIT}/glue/target/lib ${CLIENT}/
cp ${GIT}/releng/background.png /var/www/destrostudios/launcher/images/background_8.png
cp ${GIT}/releng/tile.png /var/www/destrostudios/launcher/images/tile_8.png
curl https://destrostudios.com:8080/apps/8/updateFiles
echo done
