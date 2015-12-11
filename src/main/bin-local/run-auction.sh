#!/bin/bash

if [ -z $BARATINE_HOME ]; then
  BARATINE_HOME=~/baratine
fi;

if [ ! -f $BARATINE_HOME/lib/baratine.jar ]; then
  echo "BARATINE_HOME '$BARATINE_HOME' does not point to a baratine installation";
  exit 1;
fi;

BARATINE_DATA_DIR=/tmp/baratine
BARATINE_CONF=src/main/bin-local/conf.cf
BARATINE_ARGS="--data-dir $BARATINE_DATA_DIR --conf $BARATINE_CONF"

$BARATINE_HOME/bin/baratine shutdown $BARATINE_ARGS

rm -rf $BARATINE_DATA_DIR

mvn dependency:copy -Dartifact=com.caucho:lucene-plugin-service:1.0-SNAPSHOT:bar -Dmdep.stripVersion=true -o -DoutputDirectory=$base

mvn -Dmaven.test.skip=true -P local clean package

cp  target/auction-*.bar auction.bar

$BARATINE_HOME/bin/baratine start $BARATINE_ARGS
$BARATINE_HOME/bin/baratine deploy $BARATINE_ARGS lucene-plugin-service.bar
$BARATINE_HOME/bin/baratine deploy $BARATINE_ARGS auction.bar

echo "Create User ..."
$BARATINE_HOME/bin/baratine jamp $BARATINE_ARGS --pod web /auction-session/foo createUser user pass
$BARATINE_HOME/bin/baratine jamp $BARATINE_ARGS --pod web /auction-admin-session/foo createUser admin pass

echo "Authenticate User ..."
$BARATINE_HOME/bin/baratine jamp $BARATINE_ARGS --pod web /auction-session/foo login user pass

$BARATINE_HOME/bin/baratine --verbose cat $BARATINE_ARGS /proc/services

