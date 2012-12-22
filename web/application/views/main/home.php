<div class="row">
	<!-- sidebar navigation -->
	<div class="span2">
		<div id="tag_directory"></div>
	</div>

	<!-- content -->
	<div class="span10">
		<!-- tabs -->
		<div class="tabbable">
			<ul class="nav nav-tabs">
				<li class="active"><a href="#tab_feed" id="tab_feed_link" data-toggle="tab">Read</a></li>
				<li><a href="#tab_media" id="tab_media_link" data-toggle="tab">View</a></li>
				<!-- <li><a href="#tab_map" id="tab_map_link" data-toggle="tab">Visit</a></li> -->
			</ul>
		</div>

		<!-- tab content -->
		<div class="tab-content">

			<!-- #tab_feed -->
			<div class="tab-pane active" id="tab_feed">
				<div class="row">
					<div id="feed_item_container" class="span6">
						<div class="pagination feeditems"></div>
						<div id="feed_directory"></div>
						<div class="pagination feeditems"></div>
					</div>
					<div id="tweet_feed_container" class="span4">
						<form class="well">
							<input type="text" class="span3" placeholder="Type keywords or select tags below">
							<button type="submit" class="btn btn-primary rank">Rank News</button>
						</form>
						<div id="tweet_feed_directory"></div>
					</div>
				</div>
			</div><!-- #tab_feed -->

			<!-- #tab_media -->
			<div class="tab-pane" id="tab_media">
				<div class="row">
					<div id="media_container" class="span6">
						<div class="pagination mediaitems"></div>
						<div id="media_directory"></div>
						<div class="pagination mediaitems"></div>
					</div>
					<div id="similarity_container" class="span4">
						<h3>Most similar images</h3>
						<form class="form-inline well">
							Descriptor
							<select name="select_vs_descriptor" id="select_vs_descriptor" class="input-medium">
								<option value="featureAutoColorCorrelogram">Auto Color</option>
								<option value="descriptorScalableColor">Scalable Color</option>
								<option value="featureCEDD" selected="selected">CEDD</option>
								<option value="featureColorHistogram">Color Histogram</option>
								<option value="descriptorColorLayout">Color Layout</option>
								<option value="featureTAMURA">Tamura</option>
								<option value="descriptorEdgeHistogram">Edges</option>
								<option value="featureFCTH">FCTH</option>
								<option value="featureGabor">Gabor</option>
								<option value="featureJCD">JCD</option>
								<option value="featureJpegCoeffs">Jpeg Coeffs</option>
							</select>
							<!-- still need to sort out creation of histograms for: featureSift, featureSurf -->
							<!-- 
							Size
							<div class="btn-group" data-toggle="buttons-radio">
							<button type="button" class="btn">All</button>
							<button type="button" class="btn active">Medium</button>
							<button type="button" class="btn">Big</button>
							</div>
							-->
						</form>
						<div class="loader">Loading</div>
						<div id="similarity_directory"></div>
					</div>
				</div>
			</div><!-- #tab_media -->

			<!-- #tab_map -->
			<!-- <div class="tab-pane" id="tab_map">
				<div id="map_canvas"></div>
			</div> -->
			<!-- #tab_map -->
		</div>
	</div>
</div>

<?php $this->load->view('modal_permalink'); ?>