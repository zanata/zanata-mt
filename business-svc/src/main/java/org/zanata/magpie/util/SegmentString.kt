package org.zanata.magpie.util

import org.zanata.magpie.api.dto.LocaleCode
import java.text.BreakIterator
import java.util.*

/**
 * Segments a paragraph into sentences.
 */
fun segmentBySentences(paragraph: String,
        localeCode: Optional<LocaleCode>): List<String> {
    if (paragraph.isBlank()) return emptyList()

    val locale = if (localeCode.isPresent)
        Locale(localeCode.get().id)
    else
        Locale.getDefault()
    val sentenceIterator = BreakIterator.getSentenceInstance(locale)
    return sentenceIterator.elements(paragraph)
}

// NB this was developed for sentence BreakIterator. YMMV with others.
private fun BreakIterator.elements(text: String): List<String> {
    this.setText(text)
    val elements = ArrayList<String>()
    var startBound = this.first()
    var endBound = this.next()
    while (endBound != BreakIterator.DONE) {
        elements.add(text.substring(startBound, endBound))
        startBound = endBound
        endBound = this.next()
    }
    return elements
}
