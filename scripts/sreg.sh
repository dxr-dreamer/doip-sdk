#!/bin/bash

if [ ! -d "./log" ];
then
  mkdir log
fi

if [ -a "./SREGPID" ];
then
  echo "shutdown old client"
  kill -9 `cat SREGPID`
  rm ./SREGPID
fi

nohup java -Dfile.encoding=UTF-8 -cp "./libs/*:DoipSDK-1.0.jar" org.bdware.doip.application.SimpleRegistryMain 1 > ./log/sreg.log 2 > ./log/sreg.err &

echo $! > ./SREGPID