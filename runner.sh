#!/bin/bash
rep=input/$1
sub=output/$1
FILES=$rep/*.csv
for f in $FILES
do
  tar=${f/$rep/$sub}
  echo "Processing $f to $tar..."
  target/start $f $2 $3 $4 > $tar 
  # take action on each file. $f store current file name
done
