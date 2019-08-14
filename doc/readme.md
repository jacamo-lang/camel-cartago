# JaCaMo Artifact Component
## Overview
This project contains an implementation of a custom Apache Camel component that gives agents, from a MAS JaCaMo project, a way to interact with other services as environmental artifacts.
Apache Camel is a framework based on Enterprise Integration Patterns that aims to resolve  integration problems between components.

Camel's approach gives Endpoints to each component and transmits messages encapsulated in Exchange objects.  
When a component should receive data, it uses a Consumer, that consumes the data and encapsulates it in an Exchange object to be processed. In an analog way, when a component must deliver data, the former retrieves the latter by processing an Exchange object with a Producer, and then produces the data in a proper target format.  
The JaCaMo Artifact Component gives MAS agents the perception of other services as artifacts, and the ability to interact with them by simply calling registered operations, with `{operationName}(...)` in Jason.
The following image better represents the stated:

![Communication Flow](images/CommunicationFlow.pdf?raw=true)

### Table of contents
1. [Getting Started](#getting-started)
  * [Prerequisites](#prerequisites)
  * [How to use](#how-to-use)
  * [Defining a context route](#defining-a-context-route)
  * [Using custom context configuration](#using-custom-context-configuration)
2. [Notes](#notes)
3. [Examples](#examples)
  * [Agent-only 'main'](#agent-only-'main')
  * [PostgreSQL](#postgresql)
  * [MQTT](#mqtt)


## Getting Started
### Prerequisites
1. Gradle: you can find all info on how to install gradle [Here](https://gradle.org/install/).


### How to use
1. Create a basic gradle build file and add the code below to it, or use the the `build.gradle` file whithin the main example. Note that the dependencies' version might be outdated and should be overwriten.

```
repositories {
   mavenCentral()

   maven { url "https://raw.github.com/jacamo-lang/mvn-repo/master" }
   maven { url "http://jacamo.sourceforge.net/maven2" }
}

dependencies {
  compile 'org.jacamo:jacamo:1.0-SNAPSHOT'

  compile group: 'org.jacamo-lang',     name: 'camel-cartago' ,   version: '0.1'

  compile group: 'org.apache.camel', name: 'camel-core', version: '2.22.1'

  compile group: 'org.springframework', name: 'spring-context', version: '5.0.10.RELEASE'

  compile group: 'org.slf4j', name: 'slf4j-log4j12', version: '1.7.25'

  compile group: 'org.slf4j', name: 'slf4j-api', version: '1.7.25'
}
```


2. Add the dependencies of the components you wish to integrate. Google "maven *mycomponent*", the first link should be from [this website](https://mvnrepository.com/), usually you would want the latest version, and go in the "gradle" tab, simply copy an paste in your build file. For instance, if you want to use mySQL you would add:

```
// https://mvnrepository.com/artifact/mysql/mysql-connector-java
compile group: 'mysql', name: 'mysql-connector-java', version: '8.0.13'
```

**NOTE** For JDK9+ users, see [Notes Section](##notes).

3. Create each of your contexts in separated `.xml` files and define only the routes within them. The route files should maintain the following format:

```
<!-- This first header tag is mandatory -->
<routes xmlns="http://camel.apache.org/schema/spring">
    <route id="yourRouteIdOne">
        <from uri="myComponent:address"/>
        <to uri="jacamo-artifact:myComponentName"/>
    </route>

    <route id="yourRouteIdTwo">
        <from uri="jacamo-artifact:myComponentName"/>
        <to uri="myComponent:address"/>
    </route>
</routes>
```

Each file creates a context, so you can have parellel routes running.
Check out the *Defining a context route* and *Examples* section for more.

4. Add the following to your `.jcm` project:

```
platform: artifactComponent.ArtifactCamel("routes-file-name.xml")
```

5. Run `gradle run` to start you `.jcm` with camel.

### Defining a context route
The context is defined mainly using Camel's simple language for XML.
Within the context, you can have many routes, defined by `<route> ... </route>`.

Usually, in each route you have a consumer endpoint, denoted by "from", and one or more producer endpoints, denoted by "to".

#### Producer endpoint
The purpose of the producer is to update observable properties values and/or generate signals.
When defining a producer endpoint you must use the following pattern:

`<to uri=jacamo-artifact:dummyArtifactName?optionOne=foo&optionTwo=bar/>`

Where "dummyArtifactName" is the artifact that represents the integrated component's name (e.g. database, webserver), and "optionOne" and "optionTwo" are options you can define to the message, some being mandatory.

#### Consumer endpoint
The purpose of the consumer is to register external actions as CArtAgO artifacts' operations that Jason agents can call, with or without arguments.
Defining a consumer endpoint is analog, the difference being that the producer URI is the name of the other components' representative agent. I.e. to whom the Jason agents will send the messages.

```
<from uri="jacamo-artifact:dummyArtifactName?optionOne=foo&optionTwo=bar"/>
```

#### Examples
Let's say you want to integrate a semaphor, from semaphorComponent, as an artifact to your MAS. The semaphor has an operation of requesting closure for pedestrians to cross the street, and it signals drivers and pedestrians when it changes its state. Also, drivers are able to look and perceive at any time the semaphor's light colour. In that case the artifact should have an observable property, lightColour(RED | GREEN), an operation with no arguments, requestClosure(), and emmits two signals, turnedGreen and turnedRed. Let's establish that the artifact's name should be roadSemaphor1, as we could have several.

First of all you must be sure that "semaphorComponent" is a valid camel component with its dependency imported in gradle. You can check Camel's official components [here](http://camel.apache.org/component-list-grouped.html). Some components aren't official but will work just fine.

Let's register the operation, for each operation a route should be made. Whenever an agent calls the operation 'requestClosure', the semaphor component's producer should receive the request. So: from the operation requestClosure to the semaphor's producer endpoint.

```
<route id="registeringOperation">
  <from uri="jacamo-artifact:roadSemaphor1?operation=requestClosure&amp;args=()"/>
  <to uri="semaphorComponent:semaphor1address? (...)"/>
</route>
```

Now, let's say that the semaphor component has a consumer endpoint that, whenever a physical change happens, it sends the semaphor light status (GREEN | RED) to camel router. In that scenario, the semaphor's consumer should update an observable property value and trigger the respective signal, turnedRed or turnedGreen. So from the semaphor to the observable property lightColour and to produce respective signal.
Then, you would define a route as follow:

```
<route id="observablePropertyUpdate">
  <from uri="semaphorComponent:semaphor1address? (...)"/>
  <choice>
    <when>
      <simple>{in.body} contains "GREEN"</simple>
      <to uri="jacamo-artifact:roadSemaphor1?property=lightColour&amp;value=GREEN"/>
      <to uri="jacamo-artifact:roadSemaphor1?isSignal=true&amp;property=turnedGreen"/>
    </when>
    <when>
      <simple>{in.body} contains "RED"</simple>
      <to uri="jacamo-artifact:roadSemaphor1?property=lightColour&amp;value=RED"/>
      <to uri="jacamo-artifact:roadSemaphor1?isSignal=true&amp;property=turnedRed"/>
    </when>
  </choice>
</route>
```

And finally put that route in your context:

```
<routes xmlns="http://camel.apache.org/schema/spring">
  <route id="registeringOperation">
    <from uri="jacamo-artifact:roadSemaphor1?operation=requestClosure&amp;args=()"/>
    <to uri="semaphorComponent:semaphor1address? (...)"/>
  </route>

  <route id="observablePropertyUpdate">
    <from uri="semaphorComponent:semaphor1address? (...)"/>
    <choice>
      <when>
        <simple>{in.body} contains "GREEN"</simple>
        <to uri="jacamo-artifact:roadSemaphor1?property=lightColour&amp;value=GREEN"/>
        <to uri="jacamo-artifact:roadSemaphor1?isSignal=true&amp;property=turnedGreen"/>
      </when>
      <when>
        <simple>{in.body} contains "RED"</simple>
        <to uri="jacamo-artifact:roadSemaphor1?property=lightColour&amp;value=RED"/>
        <to uri="jacamo-artifact:roadSemaphor1?isSignal=true&amp;property=turnedRed"/>
      </when>
    </choice>
  </route>
</routes>
```


#### Routes properties and configurations

The producer might receive an exchange object to process with properties alrealdy defined, but it will always overwrite them if it was also declared in the URI, giving priority to the user.

Here's a list of possible producer properties to be defined as a URI argument:

<table class="tg">
  <tr>
    <th class="tg-baqh" colspan="3">**Producer Endpoint**</th>
  </tr>
  <tr>
    <td class="tg-0pky">Attribute's name</td>
    <td class="tg-0pky">Default value</td>
    <td class="tg-0pky">Description</td>
  </tr>
  <tr>
    <td class="tg-0pky">property</td>
    <td class="tg-0pky">*null*</td>
    <td class="tg-0pky">the observable property or signal name and value (e.g. size(10), mailbox(bob, 3));</td>
  </tr>
  <tr>
    <td class="tg-0pky">isSignal</td>
    <td class="tg-0pky">true</td>
    <td class="tg-0pky">flag to make the property persistent (observable property) or as a signal;</td>
  </tr>
  <tr>
    <td class="tg-0lax" colspan="3">Note: all the elements, despite having a default value, will give priority to the exchange's properties over the default value.<br>The priority order is as follow: URI definition &gt; exchange property &gt; default value.</td>
  </tr>
</table>

Here's also a list of possible consumer properties to be defined as a URI argument:

<table class="tg">
  <tr>
    <th class="tg-baqh" colspan="3">**Consumer Endpoint**</th>
  </tr>
  <tr>
    <td class="tg-0pky">Attribute's name</td>
    <td class="tg-0pky">Default value</td>
    <td class="tg-0pky">Description</td>
  </tr>
  <tr>
    <td class="tg-0pky">operation</td>
    <td class="tg-0pky">*null*</td>
    <td class="tg-0pky">the operation's name;</td>
  </tr>
  <tr>
    <td class="tg-0pky">args</td>
    <td class="tg-0pky">()</td>
    <td class="tg-0pky">the operation argument's names, should always be within parenthesis and comma separated (e.g. args=(accountNumber, newValue) );</td>
  </tr>
</table>

For more examples check the *Examples* section down below.

### Using custom context configuration
In Camel, you can also use custom configurations in your context. This asset is mainly used when defining Beans.
If this resource is needed, you can create a context in another `.xml` file without routes, and adding ` --my-context-beans.xml` in the path you used for the routes.
Here's an example:

This would be `beans.xml`:
```
<beans xmlns="http://www.springframework.org/schema/beans"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

  <bean id="myDearBean" class="beanClass">
    <... properties and such ...>
  </bean>
</beans>
```

And `project.jcm` would have:

```
platform: artifactComponent.ArtifactCamel("routes-file-name.xml --beans.xml")
```

### Debugging and logging
Jason Component uses two types of logger:
#### **Log4j**

It logs the project initialization more thoroughly, as well as the history of messages within the exchange object.

It is an optional feature and is imported within your gradle build file. To turn it off you can simply comment out the following snippet from the dependencies section:

```
// ------------- log dependencies -------------
// https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
compile group: 'org.slf4j', name: 'slf4j-log4j12', version: '1.7.25'

// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
compile group: 'org.slf4j', name: 'slf4j-api', version: '1.7.25'
```

This dependency is usefull for testing and debugging your routes. You can define a producer that won't affect the exchange object using `<log message=""/>`.

Here's an example:

```
<routes xmlns="http://camel.apache.org/schema/spring">
    <route id="loggingRoute">
      <from uri="jacamo-artifact:myPhone?operation=call&amp;args=(number)"/>
      <log message="Making call!"/>
      <to uri="phone:myPhoneAddress?..."/>
    </route>
</routes>
```

##################### STOPPED HERE, REVIEW LOG

If your run it, in your terminal you would expect the following:

* Lots of creation and definition messages from [JasonCamel] and [    main]


* `[JasonConsumer] Message received: <mid1,alice,tell,myPhone,Message>` *<- The message was consumed.*
* `[JasonEndpoint] Generating exchange object from: <mid1,alice,tell,myPhone,Message>` *<- The consumer sent the message for processing to become an Exchange object for the upcoming producer(s)*
* `[JasonEndpoint] Message's properties: {performative=tell, msgId=mid1, source=alice}` *<- Exchange's properties.*
* `[JasonEndpoint] Message's body      : Message` *<- Exchange's message.*
* `[JasonConsumer] Exchange created for processing: Exchange[]` *<- The Exchange was successfully created and processed.*
* `[     alice] loggingRoute         INFO  Incoming call from alice!`  *<- Logging. The 'alice' in the begining is because the Exchange's source property is 'alice'.*
* `[JasonProducer] Jason message: <<auto>1,alice,tell,bob,received(myPhone, alice)>` *<- The producer generated a Jason Message with the shown poperties.*
* `[JasonProducer] Agent received`

* Logging from phone component

#### **Internal Logger**

The Java logger class is also used as an internal logger, that will log the creation of endpoint, the consumption and production of exchange objects, and errors.

In the previous snippet, the log entries that begins with [JasonCamel], [JasonEndpoint], [JasonConsumer] and [JasonProducer] are generate from the internal logger, while the entries that have a lot of white spaces in the source, as in [&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;main] for example, are generated by Log4j.


## Notes:
* If you are running your project project with JDK 9 and over, you also need the JAX-B dependency, for binding your `.xml` files. Just add the following to your gradle dependencies section:

```
// https://mvnrepository.com/artifact/javax.xml/jaxb-api
compile group: 'javax.xml', name: 'jaxb-api', version: '2.1'
```

* For a sample of gradle build file, use `build.gradle` from this folder.
* See testeRota.xml for an example. Run it with `gradle run` from this folder.
* You can visit [this page](http://camel.apache.org/simple.html) for documentation on Camel's Simple language, to better define your routes.

## Examples
### Agent-only 'main'
Although this example does not use other camel components, it is good to ilustrate the behaviour of JasonComponent in different situations.
To use you simply run `gradle run`, the routes are defined in `routeMain.xml` and `routeMain2.xml`.
In the `.xml` file, you can identify only one Jason Consumer endpoint, and several Jason Producer endpoints. That's to show that you can filter your messages through different properties, like its source, or if it is a reply to another message.
It's important to note: if you want different agents to send to the same component, you **must use a single producer**. This is better explained in *Defining a context* section above.
Note in `routeMain2.xml` that is possible to filter a message with the consumer's URI or with the <when> selector.

// REVIEW: rodou certinho, mas fiquei um pouco perdido na depuracao do codigo, talvez daria pra descrever o que e esperado que os agentes facam, o que tu acha? ai fica mais claro o que esperar no debug. acho que o mesmo vale pros outros exemplos

### PostgreSQL
This is an example that represents the use and need of custom context files.
This example uses a local PostgreSQL server, so you won't be able to use without having a running server, changing the configurations in `applicationContext.xml` and the table queried in the `routeSQL.xml` file.
To run use `gradle sql`, the routes are defined in `routeSQL.xml`.
Notice that the project, defined in `sql.jcm`, uses a custom context configuration.
To connect with an SQL database, each database managing technology (e.g. PostgreSQL, mySQL) provides its driver class that is used by Apache to establish the connection. Also, the data delivered from the DB manager must be parsed for the agents, in a Java class.
Such classes are programmed in Java must be called through beans, that need to be defined in the context configuration file.
You can look the `applicationContext.xml` file and notice the beans being defined, and one is from a custom class. When defining your own beans classes, you also need to put them inside a source folder, usually in `src/java`.
A more thorough example can be seen [here](https://dzone.com/articles/data-transformation-csv-to-xml-using-apache-camel).

### MQTT

The MQTT example is also important since it demonstrate an use of message transformation.
This example uses a local mqtt broker. You can easily create one with mosquitto [here](http://mosquitto.org/).
To run use `gradle mqtt`, the routes are defined in `routeMQTT.xml`.
The broker sends the published data in a byte array format. So that the agents could received the information, `convertBodyTo` method was used.
Different components may send data in specific formats. Usually you will need to use custom bean methods, and parse the data with Java, you can see an example of this in the *PostgreSQL* example.
