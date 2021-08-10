# Spark Streaming application processing tweets with sentiment analysis stored in Elasticsearch
## Autor: Fco. Javier Lahoz Sevilla
## Description: Spark Streaming application for process tweets and store it into Elasticseach. The application got the following information:
##### Sentiment Analysis
##### Hashtags
##### Followers
##### Geolocation
### The application was tested with the following technologies:
###### Java 1.7
###### Hadoop 2.7.1
###### Spark 2.1.0
###### Elasticsearch 2.1.0
###### Kibana 4.3.0
#
## To runnig it:
###### - First assembly the application: sbt/sbt assembly
###### - Start Hadoop, Elasticsearch, kibana
###### - put data/words/* into HDFS in the same directory (data/words)
###### - Create index in Elastic (changed yyyy-mm-dd for current date):
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
###### - Start the application replaced Twitter credentials in script: start_app.sh
#
## We can create the following Dashboards in Kibana:
<img width="1280" alt="screen shot 2016-01-12 at 23 14 34" src="https://cloud.githubusercontent.com/assets/8615818/12278667/9f9c8840-b982-11e5-9c40-a042b65c8a45.png">
<img width="1277" alt="screen shot 2016-01-12 at 23 15 17" src="https://cloud.githubusercontent.com/assets/8615818/12278739/fbeb88a8-b982-11e5-99f9-468e6f7dd7da.png">
