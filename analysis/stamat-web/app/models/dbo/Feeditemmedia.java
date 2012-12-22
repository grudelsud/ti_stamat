package models.dbo;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity
@Table(name="feeditemmedia")
public class Feeditemmedia extends Model {

	/**
	 * int(11) unsigned NOT NULL AUTO_INCREMENT
	 */
	@Id
	public long id;

	/**
	 * int(11) unsigned NOT NULL
	 */
	public long feeditem_id;

	/**
	 * int(11) unsigned NOT NULL DEFAULT '0'
	 */
	public long scraper_id;

	/**
	 * varchar(500) DEFAULT NULL
	 */
	@Constraints.MaxLength(500)
	public String url;

	/**
	 *  varchar(500) DEFAULT NULL
	 */
	@Constraints.MaxLength(500)
	public String type;

	/**
	 *  tinyint(4) NOT NULL DEFAULT '0'
	 */
	@Column(name="`primary`")
	public int primary;

	/**
	 *  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
	 */
	public Timestamp created;

	/**
	 *  tinyint(4) NOT NULL DEFAULT '0'
	 */
	public int flags;

	/**
	 *  varchar(100) DEFAULT NULL
	 */
	@Constraints.MaxLength(100)
	public String hash;

	/**
	 *  varchar(500) DEFAULT NULL
	 */
	@Constraints.MaxLength(500)
	public String abs_path;

	/**
	 *  int(11) unsigned NOT NULL DEFAULT '0'
	 */
	public long width;

	/**
	 *  int(11) unsigned NOT NULL DEFAULT '0'
	 */
	public long height;
	
	/**
	 * helper, as defined on play! documentation here: http://www.playframework.org/documentation/2.0.2/JavaEbean
	 */
	public static Finder<Long, Feeditemmedia> find = new Finder<Long, Feeditemmedia>(Long.class, Feeditemmedia.class);
}
