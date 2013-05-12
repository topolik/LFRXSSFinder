LFRXSSFinder
============

Liferay XSS Finder is a tool for finding XSS bugs in the Liferay portal project.

News
====
* 12.05.2013 - Initial commit

Documentation
=============

Local Build & Deploy
--------------------
* Call `mvn clean package'
* Copy target/XSSFinder-*.jar into Liferay source directory (the directory where are portal-impl, portal-web, portal-service ...), e.g. `cp target/XSSFinder*.jar /opt/liferay.git'
* Copy build-jsps.xml into the Liferay source directory, e.g. `cp build-jsps.xml /opt/liferay.git'

Running
-------
* Go to the Liferay source directory, e.g. `cd /opt/liferay.git'
* Run ant on the custom XML file: `ant -f build-jsps.xml'

Other Info
----------
(c) Copyright 2013 Tomáš Polešovský

