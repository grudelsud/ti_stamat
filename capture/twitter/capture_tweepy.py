import argparse
import ConfigParser

import json
import tweepy

from pymongo import Connection

class DBConnector:
	"DBConnector class to store data on a mongodb instance"

	def __init__(self):
		self.connection = Connection()
		self.db = self.connection['stamat']
		self.tweets = self.db['tweets']

	def store_tweet(self, data):
		self.tweets.insert(data)

class StreamListener(tweepy.StreamListener):
	"StreamListener class to listen stuff from twitter and save data somewhere"

	def __init__(self, store = False):

		self.store = store
		if self.store:
			self.dbconnection = DBConnector()

	def on_data(self, data):

		try:
			# checking whether data is a real tweet as it should contain the string 'in_reply_blabla'
			if self.store and 'in_reply_to_status_id' in data:
				data_obj = json.loads(data)
				self.dbconnection.store_tweet(data_obj)
				print 'saved'
			else:
				print data
		except Exception, e:
			raise e

class Main:
	"Main class to setup twitter oauth credentials and make the whole damn thing run"

	def __init__(self, args):

		self.args = args

		config = ConfigParser.RawConfigParser(allow_no_value=True)
		config.readfp(args.config)

		section_oauth = 'OAuth'
		section_filteroptions = 'Filter options'

		self.oauth = dict(config.items(section_oauth))
		self.filteroptions = dict(config.items(section_filteroptions))

		self.auth = tweepy.auth.OAuthHandler(self.oauth['consumer_key'], self.oauth['consumer_secret'])
		self.auth.set_access_token(self.oauth['access_token'], self.oauth['access_token_secret'])

		self.tweepy_api = tweepy.API(self.auth)

	def run(self):
		self.listener = StreamListener(args.store)
		self.streamer = tweepy.Stream(self.auth, self.listener)

		if self.args.filter:
			# follow=None, track=None, async=False, locations=None, count=None
			follow = self.filteroptions['follow'].split(',') if self.filteroptions['follow'] is not None else None
			track = self.filteroptions['track'].split(',') if self.filteroptions['track'] is not None else None
			locations = map(float, self.filteroptions['locations'].split(','))
			self.streamer.filter(follow, track, False, locations, self.filteroptions['count'])
		elif self.args.sample:
			self.streamer.sample()

def main(args):
	m = Main(args)
	m.run()

# setup args and entry points
parser = argparse.ArgumentParser(description="Fetch & store tweets using the streaming API")
group = parser.add_mutually_exclusive_group(required=True)

group.add_argument('-f', '--filter', action='store_true', help="fetch filtered tweets, options must be set in settings.cfg")
group.add_argument('-s', '--sample', action='store_true', help="fetch twitter's selection of tweets")

parser.add_argument('-c', '--config', type=argparse.FileType('r'), default='settings.cfg', help="full path of config file to be used, defaults to settings.cfg")
parser.add_argument('-S', '--store', action='store_true', help="store data on a mongodb instance, connection details must be defined in settings.cfg. if not defined, defaults to FALSE")

args = parser.parse_args()

if __name__ == '__main__':
	main(args)
