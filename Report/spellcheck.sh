#!/bin/bash

spellcheck() {
  echo Spellchecking $1
  aspell -c $1 --per-conf=./aspell.conf
}

export -f spellcheck
find . -name '*.tex' -not -name 'preamble.tex' -exec bash -c 'spellcheck "$0"' {} \;
