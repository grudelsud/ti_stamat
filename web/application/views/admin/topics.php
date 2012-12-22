<div class="row">

	<div id="feed_container" class="span12">
		<div id="vocabulary_detail">

			<!-- as seen from diffbot -->
			<div class="row">
				<div class="span12">
				<h3>keywords <button type="button" id="slide_keywords" class="slide">toggle</button></h3>
				<div id="keywords"></div>
				</div>
			</div>

			<!-- stamat entity extractor -->
			<div class="row">
				<div class="span4">
				<h3>people <button type="button" id="slide_people" class="slide">toggle</button></h3>
				<div id="people"></div>
				</div>

				<div class="span4">
				<h3>organizations <button type="button" id="slide_organizations" class="slide">toggle</button></h3>
				<div id="organizations"></div>
				</div>

				<div class="span4">
				<h3>locations <button type="button" id="slide_locations" class="slide">toggle</button></h3>
				<div id="locations"></div>
				</div>
			</div>

			<!-- other stuff, we really don't wanna show these -->
			<!--
			<div class="row">
				<div class="span4">
				<h3>teamlife<button type="button" id="slide_teamlife" class="slide">toggle</button></h3>
				<div id="teamlife"></div>
				</div>				

				<div class="span4">
				<h3>topics <button type="button" id="slide_topics" class="slide">toggle</button></h3>
				<div id="topics"></div>				
				</div>

				<div class="span4">
				<h3>other entities <button type="button" id="slide_entities" class="slide">toggle</button></h3>
				<div id="entities"></div>
				</div>
			</div>
			-->

		</div>

		<div id="tagged_items">
			<div id="feed_pagination" class="pagination">
			</div>
			<div id="feed_content">
			</div>
		</div>	
	</div>
</div>

<?php $this->load->view('modal_permalink'); ?>