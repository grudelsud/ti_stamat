var show_vocab = ['keywords', 'locations', 'people', 'organizations'];
var selected_tags = Array();

$(function() {
	api('get_vocabularies', function(data) {
		$.each(data.success, function(key,val) {
			if( show_vocab.indexOf( val.name ) != -1 ) {
				load_tags( val.id, '#'+val.name, true );
			}
		});
	});

	$('#vocabulary_detail button.slide').click(function() {
		var id = $(this).attr('id').replace('slide_', '');
		$('#vocabulary_detail #'+id).slideToggle();
		$(this).toggleClass('closed');
	});

	$('#vocabulary_detail li span').live({
		click: function() {
			$(this).toggleClass('selected');
			selected_tags.length = 0;
			$.each($('.selected'), function(key, val) {
				selected_tags.push( $(this).attr('id').replace('tag_', '') );
			});
			if( selected_tags.length > 0 ) {
				load_pagination( selected_tags );
				show_tagged_feed_items( selected_tags, 0, feed_pagesize );
			} else {
				$('#feed_pagination').empty();
				$('#feed_content').empty();
			}
		}
	});

	$('#feed_pagination a').live('click', function(e) {
		e.preventDefault();
		$('#feed_pagination a').removeClass('selected');
		$('#feed_pagination a').removeClass('neighbor');
		$(this).addClass('selected');
		$(this).next().addClass('neighbor');
		$(this).prev().addClass('neighbor');
		var page = $(this).attr('id').replace('page_', '');
		show_tagged_feed_items( selected_tags, page, feed_pagesize );
	});
});

function load_pagination( tag_array )
{
	$('#feed_pagination').empty();
	var data = {};
	data.tag_array = tag_array;
	api('count_feed_items', function(data) {
		var paging = '';
		var side = 2;
		var total = data.success;
		var pages = total / feed_pagesize;
		for( var i = 0; i < pages; i++ ) {
			if( (i < side) || (i > pages - side) || (0 == (i + 1) % 10) ) {
				paging += '<a href="#" id="page_'+i+'" class="marker">'+(i+1)+'</a> ';
			} else {
				paging += '<a href="#" id="page_'+i+'">'+(i+1)+'</a> ';	
			}
		}
		$('#feed_pagination').append( paging );
	}, data);
}

function show_tagged_feed_items( tag_array, page, limit )
{
	var data = {};
	data.tag_array = tag_array;
	data.offset = page * limit;
	data.limit = limit;

	api('load_tagged_feed_items', function(data) {
		var $feed_content = $('#feed_content');
		$feed_content.empty();
		$.each(data.success, function(key,val) {
			var item_id = 'item_'+val.id;
			var $item = $('<div id="'+item_id+'" class="item"></div>');
			var item_controls = '<button type="button" class="show_content">show permalink content</button>';

			$item.append('<div class="item_controls">'+item_controls+'</div>');
			$item.append('<h1><a href="'+val.permalink+'">'+val.title+'</a></h1>');
			$item.append(val.description);
			$item.append('<div class="entities"></div>');
			$item.append('<p class="footer">'+val.date+'</p>');
			$feed_content.append($item);
		});
	}, data);
}