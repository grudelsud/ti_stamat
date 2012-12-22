<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

/**
* tools
*/
class Tools extends CI_Controller
{
	
	function __construct()
	{
		parent::__construct();


		$this->load->model('user_model');
		$this->load->config('ion_auth', TRUE);
		$admin_group = $this->config->item('admin_group', 'ion_auth');

		$logged_user = array();
		$this->logged_in = $this->user_model->logged_in( $logged_user );

		// set default options & output template
		$this->data['logged_user'] = $logged_user;
		$this->data['logged_admin'] = empty($logged_user['groups']) ? false : in_array( $admin_group, $logged_user['groups']);
		// set default output template
		$this->data['template'] = 'tools';
		$this->data['output'] = '';
	}

	function index()
	{
		if (!$this->user_model->logged_in())
		{
			redirect('/auth/login', 'refresh');
		}
		$this->load->view('admin_template', $this->data);
	}
	
	function create_scrapers()
	{
		$this->db->truncate('scrapers');

		/**
		 diffbot
		 */
		$auth_params = array();
		$post_params = array();
		$data = array(
			'name' => 'diffbot',
			'rest_call' => 'http://www.diffbot.com/api/article?token='.DIFFBOT_API_KEY.'&url={SCRAPE_URL}&tags&summary&stats',
			'request_type' => 'get',
			'auth_type' => 'api_key',
			'auth_params' => json_encode( $auth_params ),
			'post_params' => json_encode( $post_params )
		);
		$this->db->insert('scrapers', $data);
		$output = '<h1>diffbot</h1>'.var_export( $data, TRUE );

		/**
		 readitlater
		 */
		$auth_params = array();
		$post_params = array();
		$data = array(
			'name' => 'readitlater',
			'rest_call' => 'http://text.readitlaterlist.com/v2/text?apikey='.READITLATER_API_KEY.'&images=1&url={SCRAPE_URL}',
			'request_type' => 'get',
			'auth_type' => 'api_key',
			'auth_params' => json_encode( $auth_params ),
			'post_params' => json_encode( $post_params )
		);
		$this->db->insert('scrapers', $data);
		$output .= '<h1>read it later</h1>'.var_export( $data, TRUE );
		
		/**
		 teamlife
		 */
		$auth_params = array(
			'username' => 'guest',
			'password' => 'teamlife'
		);
		$post_params = array(
			'text' => '{TEXT}'
		);
		$data = array(
			'name' => 'teamlife-sanr',
			'rest_call' => 'http://beta.teamlife.it/sanr/ajax/extract_words.php?text={TEXT}',
			'request_type' => 'get',
			'auth_type' => 'http_login',
			'auth_params' => json_encode( $auth_params ),
			'post_params' => json_encode( $post_params )
		);
		$this->db->insert('scrapers', $data);
		$output .= '<h1>teamlife semantic annotator</h1>'.var_export( $data, TRUE );
		
		/**
		 micc-lda
		 */
		$auth_params = array();
		$post_params = array(
			'text' => '{TEXT}',
			'docurl' => '',
			'analysis' => 'topic',
			'outputformat' => 'JSON',
			'process' => 'Process',
			'numtopics' => 2,
			'numkeywords' => 3,
			'modelname' => '',
			'dburl' => $this->db->hostname,
			'dbname' => $this->db->database,
			'dbuser' => $this->db->username,
			'dbpwd' => $this->db->password,
			'entitytypes' => 'Person, Organization, Location'
		);
		$data = array(
			'name' => 'micc-lda',
			'rest_call' => 'http://fom.londondroids.com:8080/homer/Homer',
			'request_type' => 'post',
			'auth_type' => 'api_key',
			'auth_params' => json_encode( $auth_params ),
			'post_params' => json_encode( $post_params )
		);
		$this->db->insert('scrapers', $data);
		$output .= '<h1>micc lda</h1>'.var_export( $data, TRUE );

		$this->data['output'] = $output;
		$this->index();
	}

	function create_slugs()
	{
		$output = '<h1>tag slugs created</h1>';
		$query = $this->db->get('tags');
		
		$this->load->model('vocabulary_model');
		foreach( $query->result() as $row ) {
			$slug = $this->vocabulary_model->slugify( $row->name );
			$output .= $slug.'<br/>';
			$data = array('slug' => $slug);
			$this->db->where('id', $row->id);
			$this->db->update('tags', $data);
		}
		$this->data['output'] = $output;
		$this->index();
	}
}
