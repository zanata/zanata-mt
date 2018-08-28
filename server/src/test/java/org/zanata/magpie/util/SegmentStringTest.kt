package org.zanata.magpie.util

import org.junit.Test
import org.zanata.magpie.api.dto.LocaleCode
import java.util.Optional

import org.assertj.core.api.Assertions.assertThat

/**
 * @author Alex Eng[aeng@redhat.com](mailto:aeng@redhat.com)
 */
class SegmentStringTest {

    @Test
    fun segmentStringDefaultLocaleTest() {
        val text = """
            |A cooled coincidence parks the spectacular. Behind a dragon suspects the roman fashion.
            |How can the inhabited wallet provision her made journalist?
            |The opera squashes the younger advantage.""".trimMargin()
        val strings = segmentBySentences(text, Optional.empty())
        assertThat(strings).containsExactly(
                "A cooled coincidence parks the spectacular. ",
                "Behind a dragon suspects the roman fashion.\n",
                "How can the inhabited wallet provision her made journalist?\n",
                "The opera squashes the younger advantage.")
    }

    @Test
    fun segmentStringTest() {
        val text = "Das nennen Sie ein Schinken-Sandwich? Nehmen Sie das sofort weg!"
        val strings = segmentBySentences(text, Optional.of(LocaleCode.DE))
        assertThat(strings).containsExactly(
                "Das nennen Sie ein Schinken-Sandwich? ",
                "Nehmen Sie das sofort weg!")
    }

    @Test
    fun segmentTextWithInlineHTML() {
        val text = "Jaguar will sell its new XJ-6 model in the U.S. for a small fortune :-). \n" +
                "Expect to pay around USD 120ks. Custom options can set you back another \n" +
                "few 10,000 dollars. For details, go to <a href=\"http://www.jaguar.com/sales\" \n" +
                "alt=\"Click here\">Jaguar Sales</a> or contact xj-6@jaguar.com."
        val strings = segmentBySentences(text, Optional.of(LocaleCode.EN))
//        for ((i, s) in strings.withIndex()) { println("$i: $s") }

        assertThat(strings).containsExactly(
                "Jaguar will sell its new XJ-6 model in the U.S. for a small fortune :-). \n",
                "Expect to pay around USD 120ks. ",
                "Custom options can set you back another \n" +
                "few 10,000 dollars. ",
                "For details, go to <a href=\"http://www.jaguar.com/sales\" \n" +
                "alt=\"Click here\">Jaguar Sales</a> or contact xj-6@jaguar.com.")
    }
}
