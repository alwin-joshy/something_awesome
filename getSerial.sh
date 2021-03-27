#!/bin/sh

udevadm info --query=all --name=/dev/$1 | grep 'ID_SERIAL_SHORT' | cut -d'=' -f2