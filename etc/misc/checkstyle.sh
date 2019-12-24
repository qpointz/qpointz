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

echo "copy all reports"
for f in ./*/target/scalastyle-result.xml
do
    echo "Convert ${f} to ${f}.html"
    xsltproc --verbose ./etc/misc/checkstyle.xslt ${f} > "${f}.html"

	echo "Copy ${f}"
	nn=$(echo $f | sed -e 's/\//-/g' | sed -e 's/-target//g' | rev | cut -f 2- -d '.' | rev)	
	cp "${f}" "${td}/${nn}.xml"
	cp "${f}.html" "${td}/${nn}.html"
done 