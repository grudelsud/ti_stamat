$(function() {
	// on page ready load feeds in feed table
	api( 'get_feeds', load_feeds );

	api('get_vocabularies', function(data) {
		var $select = $('#vocabulary_select select');
		$.each(data.success, function(key,val) {
			$select.append('<option value="'+val.id+'">'+val.name+'</option>')
		});
		load_tags( $select.val(), '#tags', true );
	});

	$('#vocabulary_select select').change(function() {
		load_tags( $(this).val(), '#tags', true );
	});

	// on form submit, send data to server and add feed to feed table
	$('#form_add_feed').submit(function(e) {
		e.preventDefault();
		var data = $(this).serialize();
		api( 'add_feed', add_feed, data );
	});
	
	$('#fetch_feeds button.fetch').click(function() {
		var message = 'are you sure you want to fetch all the feeds manually? this operation can take several hundreds of minutes, even days, or years...';
		$('#dialog').empty().append( message ).dialog('option', {
			'title': 'confirm',
			'buttons': {
				'cancel': function() { $(this).dialog('close'); },
				'ok': function() {
					fetch_store_all_feeds();
				}
			}
		}).dialog('open');
	});
	
	// on button "add selected tags", send data to server and add tags to selected feed
	$('#add_tag').click(function() {
		var $sel_tags = $('#vocabulary_detail #tags li span.selected');
		var id_array = new Array();
		var tag_array = new Array();
		$.each($sel_tags, function(key, val) {
			id_array[key] = $(val).attr('id').replace('tag_','');
			tag_array[key] = $(val).html();
		});
		add_feed_tags( selected_feed, id_array, tag_array );
	});

	// on feed table row click show feed details below table
	$("#feeds_table tbody").click(function(e) {
		$(feeds_table.fnSettings().aoData).each(function () {
			$(this.nTr).removeClass('row_selected');
		});
		$(e.target.parentNode).addClass('row_selected');
		var position = feeds_table.fnGetPosition( e.target.parentNode );
		var feed = feeds_table.fnGetData( position );

		selected_feed.id = feed.id;
		selected_feed.position = position;

		$('#feed_detail').hide();
		show_feed_details( selected_feed.id );
	});
	
	// on button fetch feed permalinks
	$('#feed_controls button.fetch_content').click(function() {
		var feed_id = $(this).attr('id').replace('fetch_content_', '');
		var message = 'are you sure you want to fetch and store all the permalinks associated to this feed? this operation can take several hundreds of minutes, even days, or years...';
		$('#dialog').empty().append( message ).dialog('option', {
			'title': 'confirm',
			'buttons': {
				'cancel': function() { $(this).dialog('close'); },
				'ok': function() {
					fetch_store_all_permalinks( feed_id );
				}
			}
		}).dialog('open');
		
	});

	// on button delete feed
	$('#feed_controls button.delete').click(function() {
		var feed_id = $(this).attr('id').replace('delete_', '');
		var message = 'are you sure you want to delete the feed?';
		$('#dialog').empty().append( message ).dialog('option', {
			'title': 'confirm',
			'buttons': {
				'cancel': function() { $(this).dialog('close'); },
				'ok': function() {
					delete_feed( feed_id );
				}
			}
		}).dialog('open');
	});

	// on tag click, show selected
	$('#feed_tags li span').live('click', function(e) {
		$(this).toggleClass('selected');
	});
	
	// on button delete tags (delete feeds_tags association)
	$('#tag_controls button.delete').click(function() {
		var message = 'are you sure you want to delete the selected tags?';
		$('#dialog').empty().append( message ).dialog('option', {
			'title': 'confirm',
			'buttons': {
				'cancel': function() { $(this).dialog('close'); },
				'ok': function() {
					delete_feed_tags();
				}
			}
		}).dialog('open');
	});
});

function fetch_store_all_permalinks( feed_id )
{
	$('#dialog').dialog('close');

	var data = {};
	data.feed_id = feed_id;
	api('fetch_store_all_permalinks', function() {
		alert('all good');
	}, data);
}

function fetch_store_all_feeds()
{
	$('#dialog').dialog('close');

	api('fetch_store_all_feeds', function() {
		alert('all good');
	});
}

// add row to feed table
function add_feed(data)
{
	var row = {};
	
	$.each(data.success, function(key,val) {
		row.id = val.id;
		row.title = val.title;
		row.tags = '';
		row.url = val.url;
		feeds_table.fnAddData( row );
	});
}

// add tags to selected feed
function add_feed_tags(selected_feed, id_array, tag_array) {

	// prepare post object
	var data = {};
	data.feed_id = selected_feed.id;
	data.tag_id = id_array.join(',');

	// send ajax request
	api( 'add_feed_tag', function(data) {
		
		// server replied with error
		if( typeof data.error != 'undefined' ) {
			show_error_message( data.error );
		} else {
			// all good, we can update feed table + content
			var $feed_tag_list = $('#feed_tags');
			var tags_added = new Array();
			
			// append to feed content
			$.each(data.success, function(key,val) {
				$feed_tag_list.append('<li><span id="feed_tag_'+val.id+'">'+val.name+'</span></li>');
				tags_added[key] = val.name;
			});
			
			// update feed table
			var cell_content = feeds_table.fnGetData( selected_feed.position, 2 );
			if ( cell_content.length > 0 ) {
				cell_content += ', ';
			}
			cell_content += tags_added.join(', ');
			feeds_table.fnUpdate( cell_content, selected_feed.position, 2 );
		}
	}, data );
}

// load content with feed details (below feed table)
function show_feed_details( id )
{
	api( 'get_feed', function(data) {
		var $feed_detail = $('#feed_detail');
		
		// server replies with data.success object containing
		// data.success.feed - details of selected feed
		// data.success.tags - array of tags for selected feed
		if( typeof data.success.feed != 'undefined' ) {
			// populate feed meta
			var $feed_meta = $('#feed_meta');
			$feed_meta.empty().append('<h1>'+data.success.feed[0].title+'</h1><p>'+data.success.feed[0].url+'</p>');
			
			// append tags
			var $feed_tag_list = $('#feed_tags');
			$feed_tag_list.empty();
			$.each(data.success.tags, function(key,val) {
				$feed_tag_list.append('<li><span id="feed_tag_'+val.id+'">'+val.name+'</span></li>');
			});
			$('#feed_controls button.delete').attr('id', 'delete_'+id);
			$('#feed_controls button.fetch_content').attr('id', 'fetch_content_'+id);
			$feed_detail.show();
		}
	}, 'feed_id='+id );
}

// remove from db and feed table
function delete_feed( id )
{
	$('#dialog').dialog('close');
	// prepare post object
	var data = {};
	data.feed_id = id;

	api('delete_feed', function() {
		var $feed_detail = $('#feed_detail');
		$feed_detail.hide();
		feeds_table.fnDeleteRow(selected_feed.position);
	}, data);
}

function delete_feed_tags()
{
	$('#dialog').dialog('close');

	var $feed_tags = $('#feed_tags li span.selected');
	var id_array = new Array();
	$.each($feed_tags, function(key,val) {
		id_array[key] = $(val).attr('id').replace('feed_tag_', '');
	});
	// prepare post object
	var data = {};
	data.tag_id = id_array.join(',');
	// TODO: update feed table accordingly as well
	api('delete_feed_tags', function() {
		$('#feed_tags li.selected').remove();
	}, data);
}