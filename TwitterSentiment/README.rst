TwitterSentiment
=================
TwitterSentiment Analysis application.

Overview
========
An application that analyzes the sentiments of Twitter Tweets and categorizes them as either positive, negative or neutral.

Twitter Configuration
=====================
In order to utilize the TweetCollector flowlet, which pulls a small sample stream via the Twitter API, the API key and Access token must be configured.
Follow the steps at the following page to obtain these credentials: [Twitter oauth access tokens](https://dev.twitter.com/oauth/overview/application-owner-access-tokens).
These configurations must be provided as runtime arguments to the flow prior to starting it, in order to use the TweetCollector flowlet. To avoid this, see the "disable.public" argument option below.

Flow Runtime Arguments
======================
When start the ```analysis``` flow from the UI, runtime arguments can be specified.
"disable.public" - Specify any value for this key in order to disable the source flowlet ```TweetCollector```.
"oauth.consumerKey" - See ```Twitter Configuration``` above.
"oauth.consumerSecret" - See ```Twitter Configuration``` above.
"oauth.Token" - See ```Twitter Configuration``` above.
"oauth.TokenSecret" - See ```Twitter Configuration``` above.

Installation
============

Build the Application jar:
```
mvn clean package
```

Deploy the Application to a CDAP instance defined by its host (defaults to localhost):
```
bin/app-manager.sh --host [host] --action deploy
```

Start Application Flows and Procedures:
```
bin/app-manager.sh --host [host] --action start
```

Make sure they are running:
```
bin/app-manager.sh --host [host] --action status
```

Ingest sample statements:
```
bin/ingest-statements.sh --host [host]
```

Run Web UI:
```
mvn -Pweb jetty:run (optionally use -Dcdap.host=hostname and -Dcdap.port=port to point to CDAP, localhost:10000 is used by default)
```
Once the Web UI is running, it can be viewed <host>:8080/TwitterSentiment/



License
=======

Copyright © 2014 Cask Data, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
