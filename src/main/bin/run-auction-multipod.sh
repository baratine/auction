#!/bin/bash

base=`pwd`

if [ ! -f $BARATINE_HOME/lib/baratine.jar ]; then
  echo "BARATINE_HOME '$BARATINE_HOME' does not point to a baratine installation";
  exit 1;
fi;

BARATINE_DATA_DIR=/tmp/baratine
BARATINE_CONF=src/main/bin/conf-multi.cf
BARATINE_ARGS="--data-dir $BARATINE_DATA_DIR --conf $BARATINE_CONF"

$BARATINE_HOME/bin/baratine shutdown $BARATINE_ARGS

rm -rf $BARATINE_DATA_DIR

mvn -Dmaven.test.skip=true -P local clean package
cp  target/auction-*.bar auction.bar

mvn dependency:copy -Dartifact=com.caucho:lucene-plugin-service:1.0-SNAPSHOT:bar -Dmdep.stripVersion=true -o -DoutputDirectory=$base

$BARATINE_HOME/bin/baratine start $BARATINE_ARGS --server lucene
$BARATINE_HOME/bin/baratine start $BARATINE_ARGS --server auction
$BARATINE_HOME/bin/baratine start $BARATINE_ARGS --server user
$BARATINE_HOME/bin/baratine start $BARATINE_ARGS --server web

$BARATINE_HOME/bin/baratine deploy $BARATINE_ARGS lucene-plugin-service.bar
$BARATINE_HOME/bin/baratine deploy $BARATINE_ARGS auction.bar

#echo "Create User ..."
#$BARATINE_HOME/bin/baratine jamp-query $BARATINE_ARGS --pod web /auction-session/foo createUser user pass

#echo "Authenticate User ..."
#$BARATINE_HOME/bin/baratine jamp-query $BARATINE_ARGS --pod web /auction-session/foo login user pass

#$BARATINE_HOME/bin/baratine cat $BARATINE_ARGS /proc/services

