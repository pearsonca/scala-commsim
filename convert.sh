#!/bin/bash
jekyll build --config=_config.yml,_locconfig.yml
echo "built."
java -jar mathtoweb.jar -dd . -df proc.html _site/index.html
jekyll serve --config=_config.yml,_locconfig.yml -B
wkhtmltopdf http://localhost:4000/proc.html test.pdf
