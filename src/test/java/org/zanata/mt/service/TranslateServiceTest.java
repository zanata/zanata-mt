package org.zanata.mt.service;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.dao.TextFlowDAO;
import org.zanata.mt.exception.BadTranslationRequestException;
import org.zanata.mt.exception.TranslationEngineException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.Provider;

import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiRunner.class)
public class TranslateServiceTest {

    @Inject
    private TranslationService translationService;

    @Produces
    @Mock
    private TextFlowDAO textFlowDAO;

    @Test
    @Ignore
    public void testTranslate()
        throws TranslationEngineException, BadTranslationRequestException {
        when(textFlowDAO.getByHash("3e77e984ede3bbe820911dc52b9f5630"))
                .thenReturn(null);
        
        String html = getSampleArticleHTML();

        String source = "Testing";
        Locale sourceLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");

        String translation =
                translationService.translate(source, sourceLocale, targetLocale,
                        Provider.MS);
    }


    private String getSampleArticleHTML() {
        String html = "<header class=\"header\">\n" +
            "  <h1 class=\"title\">Kernel panic title</h1>\n" +
            "  <div class=\"header-meta\">\n" +
            "    <span class=\"status inprogress\" data-content=\n" +
            "    \"This solution is in progress and will be completed soon for Red Hat customers.\"\n" +
            "    data-original-title=\"\" data-placement=\"bottom\" data-toggle=\"popover\"\n" +
            "    data-trigger=\"hover\" title=\"\"><span aria-hidden=\"true\" class=\n" +
            "    \"rh-icon-process\"></span> Solution In Progress</span> - Mis à\n" +
            "    jour<time class=\"moment_date\" datetime=\"2014-08-12T02:11:46+10:00\" title=\n" +
            "    \"August 12 2014 at 2:11 AM\">August 12 2014 at 2:11 AM</time> -\n" +
            "    <div class=\"dropdown inline\">\n" +
            "      <a aria-expanded=\"false\" aria-haspopup=\"true\" data-target=\"#\"\n" +
            "      data-toggle=\"dropdown\" href=\"\" id=\"dLabel\" role=\"button\">English\n" +
            "      <span class=\"caret\"></span></a>\n" +
            "      <ul aria-labelledby=\"dLabel\" class=\"dropdown-menu\">\n" +
            "        <li>\n" +
            "          <a href=\"#\">No translations currently exist.</a>\n" +
            "        </li>\n" +
            "      </ul>\n" +
            "    </div>\n" +
            "  </div>\n" +
            "</header>\n" +
            "<section class=\"field_kcs_environment_txt\">\n" +
            "  <h2>Environnement</h2>\n" +
            "  <ul>\n" +
            "    <li>Red Hat Enterprise Linux 6</li>\n" +
            "  </ul>\n" +
            "</section>\n" +
            "<section class=\"field_kcs_diagnostic_txt\">\n" +
            "  <h2>étapes de diagnostic</h2>\n" +
            "  <ul>\n" +
            "    <li><strong>Kernel ring buffers:</strong></li>\n" +
            "  </ul>\n" +
            "  <div class=\"code-raw\">\n" +
            "    <div class=\"code-raw-toolbar\">\n" +
            "      <a class=\"code-raw-btn\" href=\"#\">Raw</a>\n" +
            "    </div>\n" +
            "    <pre><code>crash&gt; log\n" +
            "[..]\n" +
            "BUG: unable to handle kernel paging request at ffffffff2bfeb0bc\n" +
            "IP: [&lt;ffffffff810ece37&gt;] trace_find_cmdline+0x47/0xc0\n" +
            "PGD 1a87067 PUD 0\n" +
            "</code></pre>\n" +
            "  </div>\n" +
            "  <ul>\n" +
            "    <li><strong>Backtraces:</strong></li>\n" +
            "  </ul>\n" +
            "  <div class=\"code-raw\">\n" +
            "    <div class=\"code-raw-toolbar\">\n" +
            "      <a class=\"code-raw-btn\" href=\"#\">Raw</a>\n" +
            "    </div>\n" +
            "    <pre><code>crash&gt; bt\n" +
            "  R10: 0000000000000022  R11: 0000000000000246  R12: 0000000000000000\n" +
            "  R13: 00007fffe3e5a0e8  R14: 0000000000d19090  R15: 0000000000000000\n" +
            "  ORIG_RAX: 0000000000000000  CS: 0033  SS: 002b\n" +
            "</code></pre>\n" +
            "  </div>\n" +
            "  <ul>\n" +
            "    <li><strong>Dis-assembly reference:</strong></li>\n" +
            "  </ul>\n" +
            "  <div class=\"code-raw\">\n" +
            "    <div class=\"code-raw-toolbar\">\n" +
            "      <a class=\"code-raw-btn\" href=\"#\">Raw</a>\n" +
            "    </div>\n" +
            "    <pre><code>crash&gt; dis -lr trace_find_cmdline+71\n" +
            "/usr/src/debug/kernel-2.6.32-220.el6/linux-2.6.32-220.el6.x86_64/kernel/trace/trace.c: 966\n" +
            "0xffffffff810ecdf0 &lt;trace_find_cmdline&gt;:    push   %rbp\n" +
            "/usr/src/debug/kernel-2.6.32-220.el6/linux-2.6.32-220.el6.x86_64/kernel/trace/trace.c: 969\n" +
            "0xffffffff810ece37 &lt;trace_find_cmdline+71&gt;: mov    -0x7e0f90c0(,%rdi,4),%esi\n" +
            "</code></pre>\n" +
            "  </div>\n" +
            "  <ul>\n" +
            "    <li>It should have caught the too-large <code>pid</code> value, since\n" +
            "    <code>pid</code> is an <code>int</code>, 0xea83905f looks like a negative\n" +
            "    value. It seems likely there is another problem, the pid value is wrong\n" +
            "    or the <code>ent</code> pointer is wrong.</li>\n" +
            "  </ul>\n" +
            "</section>";
        return html;
    }
}
