/*
 * Copyright Nathan Jones 2012
 *
 * This file is part of Juzidian.
 *
 * Juzidian is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Juzidian is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Juzidian.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.juzidian.android;

import javax.inject.Inject;

import org.juzidian.core.Dictionary;
import org.juzidian.core.SearchQuery;
import org.juzidian.core.SearchResults;
import org.juzidian.core.SearchResultsFuture;
import org.juzidian.core.SearchType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roboguice.RoboGuice;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

/**
 * Dictionary search view which contains a {@link SearchBar} and a ListView.
 */
public class SearchView extends RelativeLayout implements DictionarySearchTaskListener, SearchTriggerListener, PageRequestListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchView.class);

	private static final int PAGE_SIZE = 25;

	@Inject
	private Dictionary dictionary;

	private SearchQuery currentQuery;

	private SearchResultsFuture currentSearchResultsFuture;

	private SearchResults currentSearchResults;

	public SearchView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.search_view, this, true);
		RoboGuice.injectMembers(context, this);
		this.getSearchBar().setSearchTriggerListener(this);
		this.getSearchResultsView().setPageRequestListener(this);
	}

	private void doSearch(final SearchQuery searchQuery) {
		this.currentQuery = searchQuery;
		this.getSearchResultsView().showLoadingIndicator(true);
		if (currentSearchResultsFuture != null) {
			currentSearchResultsFuture.cancel();
		}
		currentSearchResultsFuture = dictionary.findAsync(searchQuery);
		final DictionarySearchTask dictionarySearchTask = new DictionarySearchTask(this);
		dictionarySearchTask.execute(currentSearchResultsFuture);
	}

	private SearchBar getSearchBar() {
		return (SearchBar) this.findViewById(R.id.searchBar);
	}

	private SearchResultsView getSearchResultsView() {
		return (SearchResultsView) this.findViewById(R.id.searchResultsView);
	}

	@Override
	public void searchTriggered(final SearchType searchType, final String searchText) {
		LOGGER.debug("Search triggered: {} - {}", searchType, searchText);
		this.currentSearchResults = null;
		final SearchResultsView searchResultsView = this.getSearchResultsView();
		searchResultsView.clearSearchResults();
		if (searchType == null) {
			this.currentQuery = null;
			searchResultsView.showLoadingIndicator(false);
		} else {
			final SearchQuery searchQuery = new SearchQuery(searchType, searchText, PAGE_SIZE, 0);
			this.doSearch(searchQuery);
		}
	}

	@Override
	public void pageRequested() {
		LOGGER.debug("Page requested");
		if (this.currentSearchResults != null) {
			final SearchQuery searchQuery = this.currentSearchResults.getSearchQuery().nextPage();
			this.doSearch(searchQuery);
		}
	}

	public SearchQuery getCurrentQuery() {
		return this.currentQuery;
	}

	@Override
	public void searchComplete(final SearchResults searchResults) {
		if (searchResults == null || !searchResults.getSearchQuery().equals(this.currentQuery)) {
			return;
		}
		this.currentSearchResults = searchResults;
		final SearchResultsView searchResultsView = this.getSearchResultsView();
		if (searchResults.isLastPage()) {
			searchResultsView.setAllowMoreResults(false);
		}
		searchResultsView.addSearchResults(searchResults.getEntries());
		searchResultsView.showLoadingIndicator(false);
	}

}
