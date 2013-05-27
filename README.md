EOS HTTP Server
===============

* EOS HTTP Server is a simple multi-threaded web server written in Java
* Currently supports HTTP 1.1 **GET**, **HEAD**, **POST** and **OPTIONS** methods
* Full file directory browsing and downloading
* Basic configuration using a properties file
* Simple design, serving files efficiently

Compile
-------

EOS uses Maven - to compile it, just issue the following command:

	mvn package

This will generate a JAR file called *eos-http-server-&lt;version&gt;.jar* under the *target* directory.

Configure
---------

EOS provides simple configuration via a properties file. It looks like this:

	server.address=0.0.0.0
	server.port=8080
	server.root=~/www
	server.index.names=index.html index.htm

* ``server.address``: The address of the server
* ``server.port``: The port to listen to
* ``server.root``: The root directory of serving documents
* ``server.index.names``: The default documents to serve when you request a directory

Run
---

EOS can be ran either with the default configuration or with a specified properties file:

	java -jar eos-http-server-<version>.jar <configuration-file>

Architecture
------------

EOS architecure is similar to [Jetty's](http://wiki.eclipse.org/Jetty/Reference/Jetty_Architecture). There is a *Server*, *Connectors*, a *Handler* and a *ThreadPool*.

* ``Server``: Puts everything together, organising all components, starting the Connectors, handles the requests via the Handlers and puts/consumes jobs to/from the ThreadPool.
* ``Connectors``: Threads waiting for connections to happen. On connection, every job is placed on the ThreadPool and is waiting to be picked from a Server and handled by a Handler.
* ``Handler``: Handles a request and acts accordingly by sending the response.
* ``ThreadPool``: Where all jobs reside. A list of jobs scheduled by the Server, waiting to be handled by a Handler.