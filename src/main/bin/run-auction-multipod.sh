#!/bin/bash

target="$1"

base=`pwd`

if [ ! -f $BARATINE_HOME/lib/baratine.jar ]; then
  echo "BARATINE_HOME '$BARATINE_HOME' does not point to a baratine installation";
  exit 1;
fi;

BARATINE_DATA_DIR=/tmp/baratine
BARATINE_CONF=src/main/bin/conf-multi.cf
BARATINE_ARGS="--data-dir $BARATINE_DATA_DIR --conf $BARATINE_CONF"

killall -9 java
rm -rf $BARATINE_DATA_DIR
sync

mvn dependency:copy -Dartifact=com.caucho:lucene-plugin-service:1.0-SNAPSHOT:bar -Dmdep.stripVersion=true -DoutputDirectory=$base

mvn -Dmaven.test.skip=true -P local clean package

exit_code=$?

if [ $exit_code -ne 0 ]; then
  echo "mvn package failed"
  exit $exit_code
fi

cp  target/auction-*.bar auction.bar

if [ "osx"="$target" ]; then
  $BARATINE_HOME/bin/baratine start $BARATINE_ARGS --server auction
  $BARATINE_HOME/bin/baratine start $BARATINE_ARGS --server user
  $BARATINE_HOME/bin/baratine start $BARATINE_ARGS --server web
  sleep 1;
  $BARATINE_HOME/bin/baratine deploy $BARATINE_ARGS auction.bar --port 8085
else
  if [ "deb"="$target" ]; then
    $BARATINE_HOME/bin/baratine start $BARATINE_ARGS --server lucene
    $BARATINE_HOME/bin/baratine start $BARATINE_ARGS --server audit
    sleep 1;
    $BARATINE_HOME/bin/baratine deploy $BARATINE_ARGS lucene-plugin-service.bar --port 8089
  else
    echo "specify target 'osx' or 'deb'"
    exit 1;
  fi
fi;

#echo "Create User ..."
#sleep 1

#$BARATINE_HOME/bin/baratine jamp-query $BARATINE_ARGS --pod web /auction-session/foo createUser user pass

#echo "Authenticate User ..."
#$BARATINE_HOME/bin/baratine jamp-query $BARATINE_ARGS --pod web /auction-session/foo login user pass

#$BARATINE_HOME/bin/baratine cat $BARATINE_ARGS /proc/services
