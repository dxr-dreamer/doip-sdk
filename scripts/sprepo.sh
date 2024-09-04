#!/bin/bash

if [ ! -d "./log" ];
then
  mkdir log
fi

if [ -a "./SPRPID" ];
then
  echo "shutdown old client"
  kill -9 `cat SPRPID`
  rm ./SPRPID
fi

nohup java -Dfile.encoding=UTF-8 -cp "./libs/*:DoipSDK-1.0.jar" org.bdware.doip.application.SimplePacketRepositoryMain 1 > ./log/spr.log 2 > ./log/spr.err &

echo $! > ./SPRPID