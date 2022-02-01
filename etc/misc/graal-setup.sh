#! /bin/bash 
GRAAL_VERSION=22.0.0.2
JAVA_VERSION=11

FN="graalvm-ce-java${JAVA_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz"
DL="https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAAL_VERSION}/${FN}"


if [[ ! -f $FN ]] ; then
   echo "Downloading $JAVA_VERSION / v ${GRAAL_VERSION}"
   wget $DL
fi

DIR_NAME="graalvm-java${JAVA_VERSION}-${GRAAL_VERSION}"

if [[ -d $DIR_NAME ]] ; then 
  echo "delete $DIR_NAME"
  rm -Rf $DIR_NAME/
fi

mkdir -p $DIR_NAME
tar xfzv $FN -C $DIR_NAME/  --strip-components=1

if [[ -d /usr/lib/jvm/$DIR_NAME ]] ; then 
  echo "delete target"
  rm -Rf /usr/lib/jvm/$DIR_NAME
fi 

mv $DIR_NAME/ /usr/lib/jvm

if [[ -d /usr/lib/jvm/graalvm-$JAVA_VERSION ]] ; then 
  echo "delete symlink"
  rm /usr/lib/jvm/graalvm-$JAVA_VERSION 
fi 

echo "create symlink"
ln -s /usr/lib/jvm/$DIR_NAME /usr/lib/jvm/graalvm-$JAVA_VERSION #/usr/lib/jvm/$DIR_NAME


if [[ $(update-alternatives --list java | grep graalvm-$JAVA_VERSION | wc -l) -eq 0 ]] ; then 
   echo "install alteraniteves"
   update-alternatives --install /usr/bin/java java /usr/lib/jvm/graalvm-$JAVA_VERSION/bin/java 4
   update-alternatives --install /usr/bin/gu gu /usr/lib/jvm/graalvm-$JAVA_VERSION/bin/gu 4
fi

echo "to eselect" 
update-alternatives --config java
update-alternatives --config gu

/usr/lib/jvm/graalvm-$JAVA_VERSION/bin/gu install python 
/usr/lib/jvm/graalvm-$JAVA_VERSION/bin/gu install r
/usr/lib/jvm/graalvm-$JAVA_VERSION/bin/gu install ruby

