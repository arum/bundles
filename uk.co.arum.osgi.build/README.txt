
How to build
============

You need:
- Java 1.5+
- Apache Ant 1.7+
- Apache Ant Ivy 2+
- Flex 3 SDK, required to build:
	- uk.co.arum.osgi.amf3.sample

Procedure:
- Create a file called build.properties in your "user.home", 
	see http://java.sun.com/j2se/1.5.0/docs/api/java/lang/System.html#getProperties() 
- in the build.properties file you created add a variable called FLEX_HOME which should 
    be the root of your Flex 3 SDK installation, e.g. FLEX_HOME=/Users/brindy/Programs/flex/fles_sdk_3
- From the bundles-build folder run Ant.  The bundles will be built and placed in a 
	generated dist directory.
