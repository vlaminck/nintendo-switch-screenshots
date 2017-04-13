@Grab(group="org.twitter4j", module="twitter4j-core", version="[3.0,)")

import twitter4j.*
import java.awt.Image
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

Properties properties = new Properties()
File propertiesFile = new File('screenshots.properties')
propertiesFile.withInputStream {
    properties.load(it)
}
new ScreenshotsParser(properties).parse()

class ScreenshotsParser {

  // properties
  private File imageDirectory
  private List<String> usernames
  private int numberOfTweetsToSearch
  private Autocrop autocrop

  ScreenshotsParser(Properties properties) {
    imageDirectory  = new File(properties["imageDirectory"])
    usernames = properties.username.split(",")
    numberOfTweetsToSearch = properties["numberOfTweetsToSearch"] as int
    autocrop = new Autocrop()
    autocrop.enabled = properties["autocrop.enabled"] as boolean
    autocrop.inputDirectory = new File(properties["autocrop.directory.input"])
    autocrop.outputDirectory = new File(properties["autocrop.directory.output"])
    autocrop.hashtag = properties["autocrop.hashtag"]
    autocrop.oldImages = properties["autocrop.oldImages"] as boolean
    autocrop.x = properties["autocrop.dimension.x"] as int
    autocrop.y = properties["autocrop.dimension.y"] as int
    autocrop.w = properties["autocrop.dimension.w"] as int
    autocrop.h = properties["autocrop.dimension.h"] as int
  }

  void parse() {
    //setup download directory
    makeDirectory(imageDirectory)
    makeDirectory(autocrop.inputDirectory)
    makeDirectory(autocrop.outputDirectory)

    //do the work
    usernames.each { username ->
      filterTweets(readFromTwitter(username, numberOfTweetsToSearch)).each{tweet ->
        File download = downloadImage(tweet)
        if (autocrop.enabled && download && tweet.hashtags.contains(autocrop.hashtag)) {
          println "copying ${download.name} into ${autocrop.inputDirectory} to be cropped"
          new File(download.name, autocrop.inputDirectory) << download.bytes
        }
      }
    }
    if (autocrop.enabled) {
      crop()
    }
  }

  // twitter methods
  private List<Status> readFromTwitter(String username, int count) {
      Twitter twitter = TwitterFactory.getSingleton()
      Paging paging = new Paging(1, count);
      return twitter.getUserTimeline(username, paging);
  }

  private List<SwitchTweet> filterTweets(List<Status> statuses) {
      return statuses.findAll{fromNintendo(it)}?.collect { status ->
          SwitchTweet tweet = new SwitchTweet()
          tweet.tweetDate = status.createdAt
          tweet.imageUrl = status.mediaEntities?.first()?.mediaURL
          tweet.text = status.text
          tweet.hashtags = status.hashtagEntities.collect { it.text }
          tweet.username = status.user.screenName
          if(tweet.imageUrl) {
              return tweet
          }
      }.findAll{it} //just to make sure there are no null values
  }

  // tweet filtering helper method
  private boolean fromNintendo(Status status) {
      return status.source.contains("Nintendo Switch Share")
  }

  //download method
  private File downloadImage(SwitchTweet tweet) {
      String filename = "${tweet.username}_" + tweet.tweetDate.format("yyyy-MM-dd_HH-mm-ss") + ".jpg"
      def file = new File(filename, imageDirectory)
      if(file.exists()){
          println "Already downloaded $filename"
          return autocrop.oldImages ? file : null
      } else {
          println "Downloading to $filename"
          def fileOutputStream = file.newOutputStream()
          fileOutputStream << new URL("${tweet.imageUrl}:large").openStream()
          fileOutputStream.close()
          return file
      }
  }

  //helper classes
  private class SwitchTweet {
      String username
      String imageUrl
      Date tweetDate
      String text
      List<String> hashtags
  }

  private class Autocrop {
    boolean enabled
    File inputDirectory
    File outputDirectory
    String hashtag
    boolean oldImages
    int x
    int y
    int w
    int h
  }

  // file management helper method
  private void makeDirectory(File dir) {
    if(!dir.exists()) {
      println "Creating directory ${dir.absolutePath}"
      dir.mkdirs()
    }
  }

  private void crop() {
    if (!autocrop.enabled) {
      println("Not cropping files. enable autocrop in screenshot.properties")
      return
    }
    autocrop.inputDirectory.eachFileRecurse() { inputFile ->

      int x = autocrop.x
      int y = autocrop.y
      int w = autocrop.w
      int h = autocrop.h

      if (inputFile.name.endsWith(".jpg")) {
        println("About to crop ${inputFile.name}")
        Image src = ImageIO.read(inputFile);

        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        dst.getGraphics().drawImage(src, 0, 0, w, h, x, y, x + w, y + h, null);

        def fileName = inputFile.name.split(".jpg")[0]
        String output = "${autocrop.outputDirectory}/${fileName}.png";
        ImageIO.write(dst, "png", new File(output));
        println("Finished writing $output")
      } else {
        println("not cropping ${inputFile.name}. File must end in .jpg")
      }
    }
  }

  private void crop(String baseDirectory, String inputFileName, int x = 22, int y = 80, int w = 1200, int h = 675) {
    println("About to crop $input")
  }

}
