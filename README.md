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
- MongoDB - any recent version should work.

Deploying to Heroku
-------------------
After cloning this repository, follow [this link on adding Heroku as a remote repository](https://devcenter.heroku.com/articles/git).
Note that you will also need the MongoLab add-on installed for your application:

`heroku addons:add mongolab`

Note the use of an optional property key MONGOLAB_URI in the application.conf - on Heroku this will be set by the add-on
so that the app can connect to a hosted MongoDB.

The application makes use of secure cookies on HTTPS only (enforced by a filter in production). For correct operation of cookies set

`heroku config:set SESSION_SECURE=true`
