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

Some info on how it works
-------------------------
Project use Jasper (Tomcat JSP compiler) to translate JSPs into Java files. 

Parse the Java files and get all places where application writes an output -> all places where XSS is possible.

Filter the occurences to limit false positives:
* Loads portal Java classes to find 'safe' methods - don't return String* (e.g. ThemeDisplay.getScopeGroupId())
* Use internal processors to parse complex expressions and validate each sub-expression
* Use safe_expressions.txt to define 'safe' expressions (constants, other safe methods, ...) that are used by processors

Loads and parses taglib sources to find vulnerable taglibs and their usages as well, for now it's very very simple implementation :)

Project is very simple, doesn't use complex Java/JSP parsers and works only with RegEXP to cut down false positives.

Other Info
----------
(c) Copyright 2013 Tomáš Polešovský

