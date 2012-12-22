(function(Media) {

	Media.Model = Backbone.Model.extend({});

	Media.Collection = Backbone.Collection.extend({
		model: Media.Model
	});

	Media.Views.Main = Backbone.View.extend({});

})(readreactv.module('media'));