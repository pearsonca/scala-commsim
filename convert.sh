#!/bin/bash
jekyll build --config=_config.yml,_locconfig.yml,_topdfconfig.yml
echo "built."
java -jar mathtoweb.jar -force -dd . -df proc.html _site/index.html
jekyll serve --config=_config.yml,_locconfig.yml -B
wkhtmltopdf http://localhost:4000/proc.html test.pdf
