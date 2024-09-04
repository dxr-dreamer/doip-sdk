#!/bin/bash

if [ ! -d "./log" ];
then
  mkdir log
fi

if [ -a "./SREPPID" ];
then
  echo "shutdown old client"
  kill -9 `cat SREPPID`
  rm ./SREPPID
fi

nohup java -Dfile.encoding=UTF-8 -cp "./libs/*:DoipSDK-1.0.jar" org.bdware.doip.application.SimpleRepositoryMain 1 > ./log/srepo.log 2 > ./log/srepo.err &

echo $! > ./SREPPID