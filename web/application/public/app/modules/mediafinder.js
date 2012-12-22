(function(Mediafinder) {

	// dependency
	var Media = readreactv.module('media');

	Mediafinder.Model = Backbone.Model.extend({
		defaults: {
			media: new Media.Collection(),
			meta: {},
			params: '',
			pagesize: 30,
			page: 1
		},
		urlRoot: function() {
			var url = base_url + 'index.php/json/media/pagesize/'+this.get('pagesize')+'/' + this.get('params');
			return url;
		},
		parse: function(response) {
			var result = response.success;
			this.set({
				media: new Media.Collection(result.media),
				meta: result.meta
			});
			return null;
		}
	});

	Mediafinder.VSCollection = Backbone.Collection.extend({
		model: Media.Model,
		url: base_url + 'index.php/json/vs',
		initialize: function(models, options) {
			this.feature = options.feature || 'featureCEDD';
			this.fileidentifier = options.fileidentifier || '';
		},
		setFileidentifier: function(id) {
			this.fileidentifier = id;
		},
		setFeature: function(feature) {
			this.feature = feature || 'featureCEDD';
		},
		url: function() {
			return base_url + 'index.php/json/vs/feature/'+ this.feature +'/fileidentifier/' + this.fileidentifier;
		},
		parse: function(response) {
			if(response.success) {
				return response.success.media;
			} else {
				return null;
			}
		}
	});

	Mediafinder.Views.VSCollection = Backbone.View.extend({
		el: '#similarity_directory',
		template: assets_url+'app/templates/vs.html',
		events: {
			'mouseenter .thumbnail': 'popoverShow',
			'mouseleave .thumbnail': 'popoverHide'
		},
		initialize: function() {
			this.collection.on('reset', this.render, this);
			this.collection.on('change', this.render, this);
		},
		popoverShow: function(e) {
			$(e.target).popover('show');
		},
		popoverHide: function(e) {
			$(e.target).popover('hide');
		},
		render: function() {
			this.$el.empty();
			// we'll render the whole stringified collection here instead of delegating to the model. it's dirtier but quicker & simpler
			var view = this;
			readreactv.fetchTemplate(this.template, function(tmpl) {
				var collection_json = {};
				collection_json.media = view.collection.toJSON();
				$('#similarity_container .loader').hide();
				if(collection_json.media.length > 0) {
					view.$el.html(tmpl(collection_json));
				} else {
					view.$el.append('<div class="alert alert-error"><button class="close" data-dismiss="alert">×</button> whoops! no results</div>');
				}
			});
			return this;
		},
	});

	Mediafinder.Views.Main = Backbone.View.extend({
		el: '#media_directory',
		template: assets_url+'app/templates/mediafinder.html',
		events: {
			'click .item': 'itemSelect'
		},
		initialize: function() {
			this.model.on('change', this.render, this);
		},
		render: function() {
			var view = this;
			// Fetch the template, render it to the View element and call done.
			readreactv.fetchTemplate(this.template, function(tmpl) {
				view.$el.html(tmpl(view.model.toJSON()));
			});
			this.addPagination();
			return this;
		},
		itemSelect: function(e) {
			e.preventDefault();
			var $label = $(e.target);
			$('#content_media').removeClass('selected');
			$label.addClass('selected');

			var _fileidentifier = $label.attr('data-id');
			var _feature = $('#select_vs_descriptor').val();

			$('#similarity_directory').empty();
			$('#similarity_container .loader').show();

			var vs_result = new Mediafinder.VSCollection([], {fileidentifier: _fileidentifier, feature: _feature});
			var vs_result_view = new Mediafinder.Views.VSCollection({collection: vs_result});
			vs_result.fetch();
		},
		addPagination: function() {
			var $pagination = $('.pagination.mediaitems');
			var $list = $('<ul></ul>');
			if( this.model.get('meta').page > 1 ) {
				$list.append('<li><a href="#!/media/'+this.model.get('meta').prev+'">&larr; Previous</a></li>');
			} else {
				$list.append('<li class="disabled"><a href="#!/media/'+this.model.get('meta').prev+'">&larr; Previous</a></li>');
			}
			$list.append('<li class="disabled"><a href="#">'+this.model.get('meta').page+'/'+this.model.get('meta').count_all_pages+'</a></li>');
			if( this.model.get('meta').page < this.model.get('meta').count_all_pages ) {
				$list.append('<li><a href="#!/media/'+this.model.get('meta').next+'">Next &rarr;</a></li>');
			} else {
				$list.append('<li class="disabled"><a href="#!/media/'+this.model.get('meta').next+'">Next &rarr;</a></li>');
			}
			$pagination.empty().append($list);
		}
	});

})(readreactv.module('mediafinder'));