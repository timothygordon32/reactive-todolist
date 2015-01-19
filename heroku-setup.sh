#!/bin/sh

APP_NAME=$1

if [ "" == "$APP_NAME" ]
then
  echo "Usage: heroku-setup.sh <app-name-to-create>"
  exit 1
fi

secret() {
  printf "Enter your $1:"
  stty -echo
  read SECRET
  stty echo
  printf "\n"
}

notsecret() {
  printf "Enter your $1:"
  read NOTSECRET
}

secret "Play Application Secret"
APP_SECRET=$SECRET
notsecret "SMTP Host"
SMTP_HOST=$NOTSECRET
notsecret "SMTP Port"
SMTP_PORT=$NOTSECRET
notsecret "SMTP Auth User"
SMTP_USER=$NOTSECRET
secret "SMTP Auth Password"
SMTP_PASSWORD=$SECRET

heroku apps:create $APP_NAME
heroku git:remote -a $APP_NAME
heroku addons:add mongolab
heroku config:set APP_SECRET=$APP_SECRET SESSION_SECURE=true SECURE_SOCIAL_SSL=true SMTP_HOST=$SMTP_HOST SMTP_PORT=$SMTP_PORT SMTP_TLS=true SMTP_USER=$SMTP_USER SMTP_PASSWORD=$SMTP_PASSWORD MONGODB_INDEX_OPERATION_DELAY=20s
