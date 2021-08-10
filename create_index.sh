curl -XPUT http://172.17.0.2:9200/twitter-$(date "+%Y.%m.%d") -H 'Content-Type: application/json' -d '
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
