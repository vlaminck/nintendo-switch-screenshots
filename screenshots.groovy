@Grab(group="org.twitter4j", module="twitter4j-core", version="[3.0,)")

import twitter4j.*

String username = "mikedave_switch"
int numberOfTweetsToSearch = 25
File directory = new File("images");
File autocropDirectory = new File("autocrop/input");

void makeDirectory(File dir) {
  if(!dir.exists()) {
    println "Creating directory ${dir.absolutePath}"
    dir.mkdirs()
  }
}

//setup download directory
makeDirectory(directory)
makeDirectory(autocropDirectory)
makeDirectory(new File("autocrop/output"))

def executeCrop = false
//do the work
filterTweets(readFromTwitter(username, numberOfTweetsToSearch)).each{tweet ->
   File download = downloadImage(tweet, directory)
   if (tweet.hashtags.contains("BreathoftheWild")) {
    new File("autocrop/input/${download.name}") << download.asWritable()
    executeCrop = true
   }
}
if (executeCrop) {
  evaluate(new File("autocrop/switch-crop.groovy"))
}

//twitter methods
List<Status> readFromTwitter(String username, int count) {
    Twitter twitter = TwitterFactory.getSingleton()
    Paging paging = new Paging(1, count);
    return twitter.getUserTimeline(username, paging);
}

List<SwitchTweet> filterTweets(List<Status> statuses) {
    return statuses.findAll{fromNintendo(it)}?.collect { status ->
        SwitchTweet tweet = new SwitchTweet()
        tweet.tweetDate = status.createdAt
        tweet.imageUrl = status.mediaEntities?.first()?.mediaURL
        tweet.text = status.text
        tweet.hashtags = status.hashtagEntities.collect { it.text }
        if(tweet.imageUrl) {
            return tweet
        }
    }.findAll{it} //just to make sure there are no null values
}

boolean fromNintendo(Status status) {
    return status.source.contains("Nintendo Switch Share")
}

//download method
File downloadImage(SwitchTweet tweet, File directory) {
    String filename = tweet.tweetDate.format("yyyy-MM-dd_HH-mm-ss") + ".jpg"
    def file = new File(filename, directory)
    if(file.exists()){
        println "Already downloaded $filename"
    } else {
        println "Downloading to $filename"
        def fileOutputStream = file.newOutputStream()
        fileOutputStream << new URL("${tweet.imageUrl}:large").openStream()
        fileOutputStream.close()
    }
    return file
}

//helper class
class SwitchTweet {
    String imageUrl
    Date tweetDate
    String text
    List<String> hashtags
}
