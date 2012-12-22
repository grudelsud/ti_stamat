import urlparse
import oauth2 as oauth

class API:
	"API used to store oauth tokens and secrets"

	def __init__(self):
		# oauth application settings
		self.consumer_key = 'rNPF1vE8d4aHmzBgcVbECQ'
		self.consumer_secret = 'qEjUohjhPmRKeY98PveFvPfvxYP8WdcXntINgo2Jog'

		self.request_token_url = 'https://api.twitter.com/oauth/request_token'
		self.authorize_url = 'https://api.twitter.com/oauth/authorize'
		self.access_token_url = 'https://api.twitter.com/oauth/access_token'

		self.access_token = '36017645-wDVsX0b86XScxkZLtY7Lx6n2coEJ6gibO7FtA1jod'
		self.access_token_secret = '1I3tyA08exUOVTZm38EPTkleQ2wmzm4b3nE9Z7aSw'

		self.consumer = oauth.Consumer(self.consumer_key, self.consumer_secret)
		self.token = oauth.Token(self.access_token, self.access_token_secret)
		self.client = oauth.Client(self.consumer, self.token)

class StreamListener:
	"StreamListener is used to listen the gardenhose and do something with twitter statuses"

	def __init__(self, api = None):
		self.api = api or API()
		self.running = False

	def _run(self, params):

		url = params['url']
		method = params['method']

		if url is not None:
			self.running = True

		while self.running:
			print 'waiting %s response from %s' % (method, url)

			resp, content = self.api.client.request(url, method)
			if resp['status'] != '200':
				print 'invalid response: %s' % resp['status']
				break

			print content

	def filter(self, count = None, delimited = None, follow = None, locations = None, track = None, stall_warnings = None):
		print "filter"
		params = {'url':'https://stream.twitter.com/1/statuses/filter.json', 'method':'POST'}
		self._run(params)

	def sample(self, count = None, delimited = None, stall_warnings = None):
		print "sample"
		params = {'url':'https://stream.twitter.com/1/statuses/sample.json', 'method':'GET'}
		self._run(params)

def main():
	sl = StreamListener()
	sl.sample()

if __name__ == '__main__':
	main()