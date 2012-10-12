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
package org.juzidian.core.datastore;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.juzidian.cedict.CedictLoader;
import org.juzidian.core.DictionaryDataStore;
import org.juzidian.core.DictionaryEntry;
import org.juzidian.core.PinyinSyllable;

/**
 * A basic sequential search dictionary that does not load all words into
 * memory.
 */
public class StreamingDictionaryDataStore implements DictionaryDataStore {

	private final CedictLoader cedictLoader;

	@Inject
	public StreamingDictionaryDataStore(final CedictLoader cedictLoader) {
		this.cedictLoader = cedictLoader;
	}

	@Override
	public List<DictionaryEntry> findChinese(final String queryString) {
		return this.findWords(new HanziSearchWordCollector(queryString));
	}

	@Override
	public List<DictionaryEntry> findPinyin(final List<PinyinSyllable> pinyinSyllables) {
		return this.findWords(new PinyinSearchWordCollector(pinyinSyllables));
	}

	@Override
	public List<DictionaryEntry> findDefinitions(final String queryString) {
		return this.findWords(new DefinitionSearchWordCollector(queryString));
	}

	private List<DictionaryEntry> findWords(final SearchWordCollector wordCollector) {
		try {
			this.cedictLoader.loadEntries(wordCollector);
		} catch (final IOException e) {
			throw new RuntimeException("Failed to load cedict entries", e);
		}
		return wordCollector.getWords();
	}

	@Override
	public void add(final DictionaryEntry entry) {
		throw new UnsupportedOperationException("Cannot add entry to streaming dictionary.");
	}

}
