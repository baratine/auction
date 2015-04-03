#!/bin/bash

if [ -z $BARATINE_HOME ]; then
  BARATINE_HOME=~/baratine
fi;

if [ ! -f $BARATINE_HOME/lib/baratine.jar ]; then
  echo "BARATINE_HOME '$BARATINE_HOME' does not point to a baratine installation";
  exit 1;
fi;

BARATINE_DATA_DIR=/tmp/baratine
BARATINE_CONF=src/main/bin/conf.cf
BARATINE_ARGS="--data-dir $BARATINE_DATA_DIR --conf $BARATINE_CONF"

$BARATINE_HOME/bin/baratine shutdown $BARATINE_ARGS

rm -rf $BARATINE_DATA_DIR

mvn -Dmaven.test.skip=true -P release clean package

cp  target/auction-*.bar auction.bar

$BARATINE_HOME/bin/baratine start $BARATINE_ARGS --deploy auction.bar

echo "Create User ..."
$BARATINE_HOME/bin/baratine jamp-query $BARATINE_ARGS --pod web /auction-session/foo createUser user pass

echo "Authenticate User ..."
$BARATINE_HOME/bin/baratine jamp-query $BARATINE_ARGS --pod web /auction-session/foo login user pass

$BARATINE_HOME/bin/baratine cat $BARATINE_ARGS /proc/services

