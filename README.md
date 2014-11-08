reactive-todolist
=================

What is it?
-------------
An application that demonstrates how a simple RESTful web application can be created using Play! backed by MongoDB in a reactive fashion.

The aim is to keep it as a simple and light demonstrator with basic features that you would expect to see in a modern web application.

Initial target platform is Heroku PaaS.

Pre-requisites
--------------
- Play 2.3.x, Scala version should be bundled.
- MongoDB - any recent version should work, tested up to 2.6.4.

Deploying to Heroku
-------------------
After cloning this repository, install the Heroku toolbelt and log in with your credentials. You can then run

`heroku-setup.sh <your-app-name>`

which will prompt you for the values of additional settings needed to run the application:
* `APP_SECRET` override this to a new (secure) random value that can be generated using 
* `SMTP_HOST` your SMTP host to send out the emails for sign-up, password change, etc.
* `SMTP_PORT` the port your SMTP server will use - TLS is the protocal.
* `SMTP_USER` the user account that will be used to authenticate the connection to send mails.
* `SMTP_PASSWORD` the corresponding password.

and `your-app-name` is a unique application name Heroku - either a fresh one you have created yourself previously or
a self-selected one in the Heroku style (eg flying-wombat-1234). 

Other configuration that is set by the script without prompting:
* `SESSION_SECURE` - set to `true` by the script to make use of secure cookies on HTTPS only (enforced by a filter in 
production). 
* `SECURE_SOCIAL_SSL` - set to `true` by the script so the the SecureSocial authentication code uses encrypted
communication.
* `SMTP_TLS` - set to `true` by the script - it is assumed that mail traffic to the SMTP server will be encrypted using
TLS.

The script embodies the operations required to set up
the application as per [this link on adding Heroku as a remote repository](https://devcenter.heroku.com/articles/git).

The script adds the MongoLab add-on (free tier) to the application. This will set the `MONGOLAB_URI` which is
an override in the application.conf.
