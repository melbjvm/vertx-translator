vertx-translator
================

This is a simple demonstration app that I built for a Java meet-up presentation. It showcases some of the features of the Vert.x platform.

It consists of a web application and an associated HTTP web service that the backend calls. From the web application you can submit a list of words
and they will be sent back translated into one or more languages.

The application highlights several of Vert.x's useful features including:

* Support for multiple programming languages
* Distributed event bus
* Event bus bridging up to the browser
* Round robin load balancing of messages on the event bus
* Sock JS support
* HTTP client support
* Use of Vert.x's web server module

This app was originally written using Vert.x 1.1 but has been upgraded to work with 1.2.2.

Installation Instructions
-------------------------

1. Download this project
2. Download Vert.x 1.2.2 or later 
3. Install Vert.x's web server module from the central repository
	```
	vertx install vertx.web-server-v1.0
	```
4. Compile the java classes in the base folder
	```
	javac -cp $VERTXLIBS/vertx-platform-1.2.2.final.jar:$VERTXLIBS/vertx-lang-groovy-1.2.2.final.jar:$VERTXLIBS/vertx-core-1.2.2.final.jar *.java
	```
	_NB: $VERTXLIBS is should be replaced with the path to the lib folder of your vert.x installation_
	
Running the demo
----------------

1. Execute each command in a separate terminal session
2. Run the translator service
   ```
   vertx run TranslatorService
   ```
   Verify that it is running correctly by going to _http://localhost:8090/translate?word=house&language=french_ in your browser
3. Run the main verticle which includes will setup the web server and run the French verticle
   ```
   vertx run Main -cluster
   ```
   Verify that the website is working by going to _http://localhost:8080_ and testing out the demo with just the French language translator running
4. Run the Italian worker verticle
   ```
   vertx run Italian -cluster -cluster-port 25600
   ```
5. Run the Spanish worker verticle
   ```
   vertx run spanish.groovy -cluster -cluster-port 25000
   ```
6.  Return to the browser and rerun the demo which should now translate in all three languages (Spanish, Italian and French), alternating between words.