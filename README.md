# Spark Streaming application processing tweets with sentiment analysis stored in Elasticsearch
## Autor: Fco. Javier Lahoz Sevilla
## Description:
## Spark Streaming application for process tweets and store it into Elasticseach. The application got the following information:
##### Sentiment Analysis
##### Hashtags
##### Followers
##### Geolocation
### The application was tested with the following technologies:
###### Java 1.7
###### Hadoop 2.7.1
###### Spark 1.6
###### Elasticsearch 2.1.0
###### Kibana 4.3.0
#
## To runnig it:
###### - First assembly the application: sbt/sbt assembly
###### - Start Hadoop, Elasticsearch, kibana
###### - Create index in Elastic changed yyyy-mm-dd for current date:
curl -XPUT 'http://localhost:9200/twitter-yyyy.mm.dd' -d '
{
   "mappings" : {
    "tweets" : {
     "properties" : {
          "location" : {
            "type" : "geo_point"
          }
        }
      }
    },
    "settings" : {
      "index" : {
        "number_of_replicas" : "0",
        "number_of_shards" : "1"
      }
    }
}
'
##### - Start the application replaced Twitter credentials in script: <br> start_app.sh
##
## We can create the following Dashboards in Kibana:

