#!/bin/bash
# Proper header for a Bash script.

FILES=" *.tex
        */*.tex
        */*/*.tex"

for file in $FILES
do
  echo "Processing $file file..."
  echo -en "[LATEX] \nmaster-filename = /home/heider/github/P5/Report/master.tex" >   ${file%/*tex}/.${file##*/}.ini
done

