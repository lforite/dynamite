[![Build Status](https://travis-ci.org/louis-forite/dynamite.png?branch=master)](https://travis-ci.org/louis-forite/dynamite)

# Dynamite

A type-safe, reactive, native Scala client for the popular DynamoDB database.
 
Built from the ground up to reduce the boilerplate.


Motivations
-------------

DynamoDB is a fully managed NoSQL database created and maintained by Amazon. Just as each Amazon product, DynamoDB comes with a SDK developed in many languages including a Java SDK (which can be found [here](http://docs.aws.amazon.com/amazondynamodb/latest/gettingstartedguide/GettingStarted.Java.html)).
The Java SDK comes with a lot of features pre-baked to inter-operate nicely with DynamoDB. However, in practice using the DynamoDB Java SDK from Scala is painful. Namely :
- java futures do not offer a lot of capabilities
- too verbose
- not type-safe
- abstruse APIs
- throws exception

Dynamite tries to address those issues by proposing an easy to use client which only deals with plain Scala `Future` and `Either` types.


Minimal working example
-----------------------

```scala
case class Student(id: String, firstName: String, lastName: String) 
    
val clientConfiguration = ClientConfiguration(AwsTable("students"), AwsRegion.EU_WEST_1)
val awsCredentials = AwsCredentials(AwsAccessKey("awsAccessKey"), AwsSecretKey("awsSecretKey"))
val dynamiteClient = DynamiteClient[Student](clientConfiguration, awsCredentials)
    
dynamiteClient.get("id" -> S("studentId1")) //will yield a Future[Either[DynamoError, Option[Student]]]
```


Limitations
-----------

Currently, polymorphism is unsupported.


Contribution guidelines
-----------------------

Currently not open source. TBD


License
-------

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.