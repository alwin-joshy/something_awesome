# $1 will be the directory of the sketch and $2 will be the system name of the target board

fqbn=$(arduino-cli board list | grep "$2" | egrep -o "\b\w+:\w+:\w+")
core=$(echo $fqbn | cut -d":" -f1,2)
arduino-cli core install $core #> /dev/null
arduino-cli compile --fqbn $fqbn $1 #> /dev/null
arduino-cli upload -p /dev/$2 --fqbn $fqbn $1 #> /dev/null
