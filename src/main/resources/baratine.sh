#!/bin/bash

BARATINE_HOME=~/baratine
#BARATINE_HOME=~/appservers/baratine-0.8.7

$BARATINE_HOME/bin/baratine shutdown

rm -rf /tmp/baratine

cp=$BARATINE_HOME/lib/baratine-api.jar
cp=$cp:$BARATINE_HOME/lib/baratine.jar

src=java
src_pkg=examples/auction

rm -rf classes
mkdir classes

#javac -cp $cp -d classes src/main/$src/$src_pkg/User*.java
javac -cp $cp -d classes src/main/$src/$src_pkg/*.java

jar -cf auction.bar classes
cp auction.bar auction.jar

$BARATINE_HOME/bin/baratine start --conf src/main/resources/conf.cf
#$BARATINE_HOME/bin/baratine start --conf src/main/resources/conf.cf --deploy auction.bar

#lucene=/Users/alex/projects/baratine-github/lucene-plugin/lucene-plugin.bar
#$BARATINE_HOME/bin/baratine put $lucene /usr/lib/lucene.bar
#$BARATINE_HOME/bin/baratine put src/main/resources/lucene.cf /config/pods/lucene.cf

$BARATINE_HOME/bin/baratine put auction.bar /usr/lib/auction.bar
$BARATINE_HOME/bin/baratine put src/main/resources/auction.cf /config/pods/auction.cf


#$BARATINE_HOME/bin/baratine jamp-query --pod web /auction-session/x createUser user pass
echo "Create User ..."
$BARATINE_HOME/bin/baratine jamp-query --pod web /auction-session/foo createUser user pass

echo "Authenticate User ..."
$BARATINE_HOME/bin/baratine jamp-query --pod web /auction-session/foo login user pass

#echo "Test /..."
#$BARATINE_HOME/bin/baratine jamp-query --pod user /test test foo
#$BARATINE_HOME/bin/baratine jamp-query --pod user /test/child-test test foo


$BARATINE_HOME/bin/baratine cat /proc/services

#killall -9 firefox
#open /Applications/Firefox.app http://localhost:63342/auction/src/web/auction.html

