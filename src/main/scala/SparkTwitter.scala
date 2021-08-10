import org.apache.spark._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming._
import org.apache.spark.storage.StorageLevel
import scala.io.Source
import scala.collection.mutable.HashMap
import java.io.File
import java.util.Date
import sys.process.stringSeqToProcess
import org.elasticsearch.spark.rdd.EsSpark
import twitter4j.GeoLocation
import java.util.Calendar
import java.text.SimpleDateFormat
import org.apache.spark.rdd.RDD

object SparkTwitter {

   var apiKey = ""
   var apiSecret = ""
   var accessToken = ""
   var accessTokenSecret = ""

   case class Tweet(user_name:String,user_lang:String,text:String,created_at:Date,retweet_count:Long,country:String,location:Array[Double],isRetweet:Boolean,followers:Long,hashtag:Array[String],sentiment:Int)
   
   def configureTwitterCredentials(apiKey: String, apiSecret: String, accessToken: String, accessTokenSecret: String) {
    val configs = new HashMap[String, String] ++= Seq(
      "apiKey" -> apiKey, "apiSecret" -> apiSecret, "accessToken" -> accessToken, "accessTokenSecret" -> accessTokenSecret)
    println("Configuring Twitter OAuth")
    configs.foreach{ case(key, value) => 
        if (value.trim.isEmpty) {
          throw new Exception("Error setting authentication - value for " + key + " not set")
        }
        val fullKey = "twitter4j.oauth." + key.replace("api", "consumer")
        System.setProperty(fullKey, value.trim)
    }
  }

  def sentimentText(pos_list:scala.collection.immutable.Set[String],neg_list:scala.collection.immutable.Set[String],stopwords:scala.collection.immutable.Set[String],text:String): Int={
     var poscount = 0
     var negcount = 0
     val words = text.replaceAll("[^a-zA-Z\\s]", "").trim().toLowerCase().split(" ")
     for(word <- words){ 
       if(stopwords.contains(word)){ 
         None
       }else{
         if(pos_list.contains(word)){ 
           poscount += 1 
         } 
         if(neg_list.contains(word)){ 
           negcount += 1 
         } 
       }  
     }
     return (poscount - negcount)
  }

  def main(args: Array[String]) {

    // Get parameters: Twitter Credentials
    if (args.length == 4){
   	apiKey = args(0)
   	apiSecret = args(1)
   	accessToken = args(2)
   	accessTokenSecret = args(3)
    }else{
	println("Error usage: TwitterKey, TwitterSecretKey, TwitterToken, TwitterTokenSecret")
	System.exit(1)
    } 
    
    // Configure Twitter credentials
    configureTwitterCredentials(apiKey, apiSecret, accessToken, accessTokenSecret)

    // Configue Elasticseach connection
    val conf = new SparkConf().
      set("es.nodes","172.17.0.2:9200").
      set("es.resources","twitter").
      set("es.nodes.discovery","false").
      set("es.nodes.wan.only","true").
      set("es.index.auto.create", "true")

    val sc = new SparkContext(conf) 
    // Load sentiment words
    val pos_list_es =  sc.textFile("file:///root/twitter/data/words/pspa.txt").filter(line => !line.isEmpty()).collect().toSet
    val neg_list_es =  sc.textFile("file:///root/twitter/data/words/nspa.txt").filter(line => !line.isEmpty()).collect().toSet
    val pos_list_en =  sc.textFile("file:///root/twitter/data/words/positive-words.txt").filter(line => !line.isEmpty()).collect().toSet
    val neg_list_en =  sc.textFile("file:///root/twitter/data/words/negative-words.txt").filter(line => !line.isEmpty()).collect().toSet
    val stopwords =  sc.textFile("file:///root/twitter/data/words/stopwords.txt").filter(line => !line.isEmpty()).collect().toSet

    val ssc = new StreamingContext(sc, Seconds(5))
    val tweets = TwitterUtils.createStream(ssc, None)
    
    val today = Calendar.getInstance().getTime()
    val curTimeFormat = new SimpleDateFormat("yyyy.MM.dd")
    val date = curTimeFormat.format(today) 

    // Index name 
    val index_name = "twitter-"+date.toString()
    val type_name = "tweets" // Search Word


    tweets.foreachRDD(tweet => {
      if (tweet.isEmpty) {
        println("No tweets received in this time interval")
      }else{
	  val tweetsRdd = tweet.map(t => {

	  var country = ""
	  var lonlat : Array[Double] = Array()
	  // Get Country
          if(t.getPlace() != null){
            country = t.getPlace().getCountry()
          }
          
	  // Get Geo Location
          if(t.getGeoLocation() != null) {
            lonlat = Array(t.getGeoLocation().getLongitude(), t.getGeoLocation().getLatitude())
          }
	  // Get Hashtag
          val hashtag = t.getText().split(" ").filter(word => word.startsWith("#"))	  

	  // Get Sentiment
	  var sentiment = 0
	  if (t.getLang() == "es")
	    sentiment = sentimentText(pos_list_es,neg_list_es,stopwords,t.getText())
	  if (t.getLang() == "en")
	    sentiment = sentimentText(pos_list_en,neg_list_en,stopwords,t.getText())

	  Tweet(t.getUser().getScreenName(),t.getLang(),t.getText(),t.getCreatedAt(),t.getRetweetCount(),country,lonlat,t.isRetweet(),t.getUser().getFollowersCount(),hashtag,sentiment)
         
	})

        EsSpark.saveToEs(tweetsRdd, index_name+"/"+type_name)
       }
      }
     )

    ssc.start()
    ssc.awaitTermination()

  }
}

