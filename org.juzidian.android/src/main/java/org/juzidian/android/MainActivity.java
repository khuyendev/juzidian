package org.juzidian.android;

import java.util.List;

import org.juzidian.core.Dictionary;
import org.juzidian.core.DictionaryEntry;
import org.juzidian.core.DictionaryFactory;
import org.juzidian.core.PinyinSyllable;
import org.juzidian.core.SearchType;
import org.juzidian.core.inject.StreamingDictionaryModule;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class MainActivity extends Activity {

	private Dictionary dictionary;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		final Injector injector = Guice.createInjector(new StreamingDictionaryModule());
		final DictionaryFactory dictionaryFactory = injector.getInstance(DictionaryFactory.class);
		this.dictionary = dictionaryFactory.createDictionary();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void doSearch(@SuppressWarnings("unused") final View view) {
		final EditText searchInput = (EditText) this.findViewById(R.id.searchInput);
		final RadioGroup searchTypeRadioGroup = (RadioGroup) this.findViewById(R.id.searchTypeRadioGroup);
		final SearchType selectedSearchType = this.getSearchType(searchTypeRadioGroup.getCheckedRadioButtonId());
		final LinearLayout searchResultLayout = (LinearLayout) this.findViewById(R.id.searchResultLayout);
		searchResultLayout.removeAllViews();
		final ProgressBar progressBar = new ProgressBar(searchResultLayout.getContext());
		searchResultLayout.addView(progressBar);
		final List<DictionaryEntry> words = this.dictionary.find(searchInput.getText().toString(), selectedSearchType);
		searchResultLayout.removeAllViews();
		for (final DictionaryEntry chineseWord : words) {
			final TextView textView = new TextView(searchResultLayout.getContext());
			final String pinyinDisplay = this.createPinyinDisplay(chineseWord);
			final String englishDefinitionDisplay = this.createEnglishDefinitionDisplay(chineseWord);
			textView.setText(chineseWord.getTraditional() + " (" + pinyinDisplay + "): " + englishDefinitionDisplay);
			searchResultLayout.addView(textView);
		}
	}

	private SearchType getSearchType(final int searchTypeRadioButtonId) {
		switch (searchTypeRadioButtonId) {
		case R.id.pinyinSearchTypeRadio:
			return SearchType.PINYIN;
		case R.id.reverseSearchTypeRadio:
			return SearchType.REVERSE;
		case R.id.hanziSearchTypeRadio:
			return SearchType.HANZI;
		}
		throw new IllegalArgumentException("Invalid search type radio button id: " + searchTypeRadioButtonId);
	}

	private String createEnglishDefinitionDisplay(final DictionaryEntry chineseWord) {
		final List<String> definitions = chineseWord.getDefinitions();
		final StringBuilder stringBuilder = new StringBuilder();
		for (final String definition : definitions) {
			stringBuilder.append(definition).append("; ");
		}
		return stringBuilder.substring(0, stringBuilder.length() - 2);
	}

	private String createPinyinDisplay(final DictionaryEntry chineseWord) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (final PinyinSyllable syllable : chineseWord.getPinyin()) {
			stringBuilder.append(syllable.getDisplayValue()).append(" ");
		}
		return stringBuilder.toString().trim();
	}

}