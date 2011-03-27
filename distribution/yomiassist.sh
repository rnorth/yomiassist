#!/bin/sh

java -cp lib/ -jar yomiassist*.jar $@ -o generated.html

wkhtmltopdf --page-width 8.5cm --page-height 11.4cm --debug-javascript generated.html ebook.pdf