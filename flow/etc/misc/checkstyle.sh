#!/bin/bash 

ls -lac ./

echo "converting all reports to html"

td=./target/scalastyle

if [ -d ${td} ]; then 
	echo "deleting target dir"
	rm -Rf ${td}
fi 

echo "creating empty out dir"
mkdir -p ${td}

shopt -s globstar
echo "copy all reports"
for f in ./**/*/target/scalastyle-result.xml
do
    outf="${td}/$(echo $f | sed -e 's/\//-/g' | sed -e 's/\.-//g' | sed -e 's/-target//g' | sed -e 's/-result\.xml/\.html/g')"
    echo "Convert ${f} to ${f}.html"
    xsltproc ./etc/misc/checkstyle.xslt ${f} > "${outf}"
done 
