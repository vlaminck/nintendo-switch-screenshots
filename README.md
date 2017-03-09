# Nintendo Switch Screenshot Downloader

I like being able to share screenshots from my Nintendo Switch to Twitter, but I'd also like to download a copy. This groovy script reads your timeline and downloads the screenshots to your computer.

## Twitter setup

I have a dedicated twitter account that I use only for posting screenshots from the Nintendo Switch. In my case it is marked as private, and my main twitter account has access.

In theory this script will work with any Twitter account, but I've only tested with this setup. I do test for tweets that come from the Nintendo Switch, so that should work.

## Twitter Auth

To use this, you will need to create a new Twitter application at https://apps.twitter.com/. This is what will be used to lookup the tweets. Once you setup the application you will need to create yourself an access token. In my case, I used my main twitter account to set this up, since it knows about my "screenshot-only" account's tweets.

Once you have the application keys and access tokens for your new app, create a `twitter4j.properties` file in the root directory in this format:

	debug=true
	oauth.consumerKey=*********************
	oauth.consumerSecret=******************************************
	oauth.accessToken=**************************************************
	oauth.accessTokenSecret=******************************************

More information can be found here: http://twitter4j.org/en/configuration.html#fileconfiguration

## Running

First update the `screenshots.groovy` file. Towards the top there are a few configuration options including what username.

Then just run:

	groovy screenshots.groovy

It pulls the last 10 (or whatever you changed it to) tweets from your user, and downloads the image. It will not download the same image twice because it names the files based on the date of the tweet.
