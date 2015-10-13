#!/bin/sh

spellcheck() {
  echo ------- Spellchecking $1 ----------
  #cat $1 | aspell -a --per-conf=./aspell.conf | grep -v -E '\*|International Ispell Version' | uniq
  aspell -c $1 --per-conf=./aspell.conf # | grep -v -E '\*|International Ispell Version' | uniq
}

export -f spellcheck
find . -name '*.tex' -not -name 'preamble.tex' -exec bash -c 'spellcheck "$0"' {} \;
