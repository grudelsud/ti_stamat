<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class Main extends CI_Controller {

	function __construct()
	{
		parent::__construct();

		// $this->output->enable_profiler(TRUE);

		$this->load->model('user_model');
		$this->load->config('ion_auth', TRUE);
		$admin_group = $this->config->item('admin_group', 'ion_auth');
		$logged_user = array();
		$this->logged_in = $this->user_model->logged_in( $logged_user );

		// set default options & output template
		$this->data['logged_user'] = $logged_user;
		$this->data['logged_admin'] = empty($logged_user['groups']) ? false : in_array( $admin_group, $logged_user['groups']);

		if ($this->session->userdata('detail_needed')){
			$this->data['template'] = 'user_detail';
			$this->update_user();
			$this->session->set_userdata('detail_needed', false);
		} else {
			$this->data['template'] = 'home';
		}
	}

	function wipe()
	{
		$this->session->sess_destroy();
		redirect('/', 'refresh');
	}

	function index()
	{
		if(!$this->logged_in) {
			redirect('/auth/login', 'refresh');
		}
		$this->load->view('main_template', $this->data);
	}

	function update_user() {
		$this->load->library('ion_auth');
		$this->load->library('form_validation');
		//validate form input
		$this->form_validation->set_rules('username', 'Username', 'required|xss_clean');
		$this->form_validation->set_rules('email', 'Email Address', 'required|valid_email');
		$this->form_validation->set_rules('password', 'Password', 'required|min_length[' . $this->config->item('min_password_length', 'ion_auth') . ']|max_length[' . $this->config->item('max_password_length', 'ion_auth') . ']|matches[password_confirm]');
		$this->form_validation->set_rules('password_confirm', 'Password Confirmation', 'required');
		if ($this->form_validation->run() == true)
		{
			$username = $this->input->post('username');
			$email = $this->input->post('email');
			$password = $this->input->post('password');
			$id = $this->session->userdata('user_id');
			$data = array(
				'username' => $username,
				'email' => $email,
				'password' => $password,
			);
			$this->ion_auth->update($id, $data);
			$this->logged_in = $this->ion_auth->login($email, $password);
			redirect('/', 'refresh');
		} else {
			//display the create user form
			//set the flash data error message if there is one
			$this->data['message'] = (validation_errors() ? validation_errors() : ($this->ion_auth->errors() ? $this->ion_auth->errors() : $this->session->flashdata('message')));
			$this->data['username'] = array(
				'name' => 'username',
				'id' => 'username',
				'type' => 'text',
				'value' => $this->form_validation->set_value('username'),
			);
			$this->data['email'] = array(
				'name' => 'email',
				'id' => 'email',
				'type' => 'text',
				'value' => $this->form_validation->set_value('email'),
			);
			$this->data['password'] = array(
				'name' => 'password',
				'id' => 'password',
				'type' => 'password',
				'value' => $this->form_validation->set_value('password'),
			);
			$this->data['password_confirm'] = array(
				'name' => 'password_confirm',
				'id' => 'password_confirm',
				'type' => 'password',
				'value' => $this->form_validation->set_value('password_confirm'),
			);
			// set username equal to the screen_name of the twitter account
			if ($this->session->userdata('detail_needed')){
				$this->data['username']['value']=$this->data['logged_user']['username'];
			}
			$this->data['template'] = 'user_detail';
			if ($this->data['message']!=false){
				$this->load->view('main_template', $this->data);
			}
		}
	}

	// this clearly is not the right place for this function, it's just that I don't want to add code to someone else's classes for maintenance (ion_auth in this case)
	function login_facebook()
	{
		$config = array(
			'appId' => FB_APP_ID,
			'secret' => FB_APP_SECRET,
			'fileUpload' => TRUE
		);
		$this->load->library('Facebook', $config);
		$user = $this->facebook->getUser();
		$profile = null;
		if( $user ) {
			try {
				// Proceed knowing you have a logged in user who's authenticated.
				$profile = $this->facebook->api('/me?fields=id,name,link,email');
				$this->load->library('ion_auth');
				$login = $this->ion_auth->login($profile['email'], $profile['id']);
				if( !$login ) {
					$this->ion_auth->register($profile['name'], $profile['id'], $profile['email']);
					$this->logged_in = $this->ion_auth->login($profile['email'], $profile['id']);
				}
			} catch (FacebookApiException $e) {
				// TODO: I'm sure we should do something here
			}
		}
		redirect('/', 'refresh');
	}

	// function to allow users to log in via twitter
	function login_twitter()
	{
		$config = array(
			'consumer_key'  => TWITTER_COSUMERKEY,
			'consumer_secret' => TWITTER_CONSUMERSECRET,
			'oauth_token' => NULL,
			'oauth_token_secret' => NULL
		);
		$this->load->library('Twitteroauth',$config);
		// Requesting authentication tokens, the parameter is the URL we will be redirected to
		$request_token = $this->twitteroauth->getRequestToken(BASE_URL . 'index.php/main/oauth_twitter');

		// $this->session->set_userdata('twitter_debug', '// got request token '.var_export($request_token, TRUE).' '.$this->session->userdata('twitter_debug'));

		// If everything goes well..
		if($this->twitteroauth->http_code==200){
		// Let's generate the URL and redirect
			$url = $this->twitteroauth->getAuthorizeURL($request_token['oauth_token']);

			$this->session->set_userdata('request_token', $request_token['oauth_token']);
			$this->session->set_userdata('request_token_secret', $request_token['oauth_token_secret']);

			redirect($url, 'refresh');
		} else {
			// It's a bad idea to kill the script, but we've got to know when there's an error.
			die('Something wrong happened.');
		}
	}

	function oauth_twitter()
	{
		$cosumerKey = TWITTER_COSUMERKEY;
		$cosumerSecret = TWITTER_CONSUMERSECRET;
		$oauth_token = $this->session->userdata('request_token');
		$oauth_token_secret = $this->session->userdata('request_token_secret');

		if(!empty($_GET['oauth_verifier']) && $oauth_token !== false && $oauth_token_secret !== false){
			$config = array(
				'consumer_key'  => TWITTER_COSUMERKEY,
				'consumer_secret' => TWITTER_CONSUMERSECRET,
				'oauth_token' => $oauth_token,
				'oauth_token_secret' => $oauth_token_secret
			);
			$this->load->library('Twitteroauth', $config);
			// Let's request the access token
			$access_token = $this->twitteroauth->getAccessToken($_GET['oauth_verifier']);
			// Save it in a session var
			$this->session->set_userdata('access_token', $access_token['oauth_token']);
			$this->session->set_userdata('access_token_secret', $access_token['oauth_token_secret']);

			// $this->session->set_userdata('twitter_debug', '// got access token '.var_export($access_token, TRUE).' '.$this->session->userdata('twitter_debug'));

			// Let's get the user's info
			$user_info = $this->twitteroauth->get('account/verify_credentials');

			$profile['name'] = $user_info->screen_name;
			$profile['email'] = $user_info->screen_name . '@twitter.com';
			$profile['id'] = $user_info->id;

			$additional_info = array(
				'screen_name' => $user_info->screen_name,
				'oauth_token' => $access_token['oauth_token'],
				'oauth_token_secret' => $access_token['oauth_token_secret']
			);

			$this->db->where('screen_name', $this->db->escape_str($user_info->screen_name));
			$query = $this->db->get('users');

			if ($query->num_rows() > 0) {
				$user = $query->row();
				$session_data = array(
					'username'        => $user->username,
					'email'           => $user->email,
					'user_id'         => $user->id,
					'old_last_login'  => $user->last_login
				);
				$this->session->set_userdata($session_data);
			} else {
				$this->session->set_userdata('detail_needed', true);
				$this->ion_auth->register($profile['name'], $profile['id'], $profile['email'], $additional_info);
				$this->logged_in = $this->ion_auth->login($profile['email'], $profile['id']);
			}
		}
		redirect('/', 'refresh');
	}
}
/* End of main.php */