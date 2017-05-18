#!/bin/bash

# A script that downloads sample images into "sample-images" directory.

LISTURL=http://szyman.magres.net/sample-images/filelist

mkdir -p sample-images
wget -P sample-images -nc -i <(wget -qO - $LISTURL) 
