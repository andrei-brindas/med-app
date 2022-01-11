#!/bin/bash

if [ $PROFILE ]
then
	echo ""
else
	PROFILE=default
fi
if [ $PORT ]
then
	echo "PORT=$PORT"
else
	PORT=8080
fi
if [ $JWT_SECURITY_KEY_F ]
then
	JWT_SECURITY_KEY="--JWT_SECURITY_KEY=$(cat $JWT_SECURITY_KEY_F)"
else
	JWT_SECURITY_KEY="--JWT_SECURITY_KEY=secret"
	echo "Change default JWT_SECURITY_KEY (secret) !"
fi

java -jar -Dspring.profiles.active=${PROFILE} app.jar --PORT=$PORT $JWT_SECURITY_KEY
