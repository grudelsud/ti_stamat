package models.dbo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.data.validation.Constraints;
import play.db.ebean.Model;


@Entity
@Table(name="users")
public class Users extends Model {

	/**
	 * `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
	 */
	@Id
	public int id;
	/**
     * `group_id` mediumint(8) unsigned NOT NULL,
	 */
	public int group_id;

	/**
     * `ip_address` char(16) NOT NULL,
	 */
	@Constraints.MaxLength(16)
	public String ip_address;
	
	/**
     * `username` varchar(15) NOT NULL,
	 */
	@Constraints.MaxLength(15)
	public String username;

	/**
     * `screen_name` varchar(15) NOT NULL,
	 */
	@Constraints.MaxLength(15)
	public String screen_name;

	/**
     * `password` varchar(40) NOT NULL,
	 */
	@Constraints.MaxLength(40)
	public String password;

	/**
     * `salt` varchar(40) DEFAULT NULL,
	 */
	@Constraints.MaxLength(40)
	public String salt;

	/**
     * `email` varchar(254) NOT NULL,
	 */
	@Constraints.MaxLength(254)
	public String email;
	
	/**
     * `activation_code` varchar(40) DEFAULT NULL,
	 */
	@Constraints.MaxLength(40)
	public String activation_code;

	/**
     * `forgotten_password_code` varchar(40) DEFAULT NULL,
	 */
	@Constraints.MaxLength(40)
	public String forgotten_password_code;

	/**
     * `remember_code` varchar(40) DEFAULT NULL,
	 */
	@Constraints.MaxLength(40)
	public String remember_code;

	/**
     * `created_on` int(11) unsigned NOT NULL,
	 */
	public long created_on;

	/**
     * `last_login` int(11) unsigned DEFAULT NULL,
	 */
	public long last_login;

	/**
     * `active` tinyint(1) unsigned DEFAULT NULL,
	 */
	public int active;

	/**
     * `oauth_token` varchar(255) DEFAULT NULL,
	 */
	@Constraints.MaxLength(255)
	public String oauth_token;

	/**
     * `oauth_token_secret` varchar(255) DEFAULT NULL,
	 */
	@Constraints.MaxLength(255)
	public String oauth_token_secret;
	
	public static Finder<Integer, Users> find = new Finder<Integer, Users>(Integer.class, Users.class);
}
