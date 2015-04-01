#!/bin/bash
cd ~/scala-commsim
module load java/1.8.0_31
module load scala
git pull
sbt start-script
