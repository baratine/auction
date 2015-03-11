#!/bin/bash

if [ -z $BARATINE_HOME ]; then
  BARATINE_HOME=~/baratine
fi;

if [ ! -f $BARATINE_HOME/lib/baratine.jar ]; then
  echo "BARATINE_HOME '$BARATINE_HOME' does not point to a baratine installation";
  exit 1;
fi;

cp=$BARATINE_HOME/lib/baratine-api.jar
cp=$cp:$BARATINE_HOME/lib/baratine.jar

rm -rf classes
mkdir classes

javac -cp $cp -d classes src/main/java/examples/auction/*.java

jar -cMf auction.bar classes -C src/main/resources META-INF

BARATINE_DATA_DIR=/tmp/baratine
BARATINE_CONF=src/main/bin/conf.cf
BARATINE_ARGS="--data-dir $BARATINE_DATA_DIR --conf $BARATINE_CONF"

$BARATINE_HOME/bin/baratine shutdown $BARATINE_ARGS

rm -rf $BARATINE_DATA_DIR

$BARATINE_HOME/bin/baratine start $BARATINE_ARGS --deploy auction.bar

echo "Create User ..."
$BARATINE_HOME/bin/baratine jamp-query $BARATINE_ARGS --pod web /auction-session/foo createUser user pass

echo "Authenticate User ..."
$BARATINE_HOME/bin/baratine jamp-query $BARATINE_ARGS --pod web /auction-session/foo login user pass

$BARATINE_HOME/bin/baratine cat $BARATINE_ARGS /proc/services

open src/web/auction.html

