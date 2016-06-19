#!/usr/bin/env bash

hot_potato_exec="./target/universal/stage/bin/hot-potato"
base_port=9000
num_players=$1
max_port=$(( $num_players + $base_port - 1))

echo "Base port" $base_port
echo "Num players" $num_players
echo "Max port" $max_port

activator compile
activator stage


for (( p=$base_port; p<=max_port; p++ )) do
    echo $hot_potato_exec -Dhttp.port=$p -Dstart_port=$base_port -Dend_port=$max_port &   
    $hot_potato_exec -Dhttp.port=$p -Dstart_port=$base_port -Dend_port=$max_port &
done


