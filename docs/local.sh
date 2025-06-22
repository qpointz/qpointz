git for-each-ref --format "%(refname)"        
echo "smv_tag_whitelist=$DOC_TAGS_WHITE_LIST"
echo "smv_branch_whitelis=$DOC_BRANCH_WHITE_LIST"

sphinx-multiversion -D "smv_tag_whitelist=$DOC_TAGS_WHITE_LIST" \
                    -D "smv_branch_whitelist=$DOC_BRANCH_WHITE_LIST" \
                     ${CI_PROJECT_DIR}/docs/source ${CI_PROJECT_DIR}/docs/build
cd ${CI_PROJECT_DIR}/docs/build

if git branch --remote --format "%(refname:lstrip=3)" | grep -E '\/'
then  
    git branch --remote --format "%(refname:lstrip=3)" | grep -E '\/' | xargs -l bash -c 'mv $0 $(echo "$0" | sed '"'"'s/\//-/g'"'"' )'
fi

find ${CI_PROJECT_DIR}/docs/build -empty -type d -delete    
multi-version-info.py ${CI_PROJECT_DIR}/docs/build ${CI_PROJECT_DIR}/docs/build/versions.json
cd ${CI_PROJECT_DIR}/docs        
cp -R ./build/main/ ./out/    
cp -R ./build/* ./out/
mv ./out/main ./out/stable
cat ${CI_PROJECT_DIR}/docs/build/versions.json
cp ${CI_PROJECT_DIR}/docs/build/versions.json ./out

for i in ./out/* # iterate over all files in current dir
do
    if [ -d "$i" ] 
    then
        cp ${CI_PROJECT_DIR}/docs/build/versions.json "$i"
    fi
done    

mv ${CI_PROJECT_DIR}/docs/out/ ${CI_PROJECT_DIR}/public/

cd ${CI_PROJECT_DIR}

tar -czf ${CI_PROJECT_DIR}/docs.tar.gz -C ${CI_PROJECT_DIR} public/   