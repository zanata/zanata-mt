package org.zanata.mt.backend.ms;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.zanata.mt.annotation.SystemProperty;
import org.zanata.mt.backend.ms.internal.dto.MSString;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayReq;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayResp;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayReqOptions;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.service.TranslatorBackend;
import org.zanata.mt.util.DTOUtil;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Service for Microsoft translator. Checks for {@link #AZURE_ID} and
 * {@link #AZURE_SECRET} during startup.
 *
 *
 * See
 * {@link #translate(String, Locale, Locale, MediaType)} and
 * {@link #translate(List, Locale, Locale, MediaType)} for more info.
 *
 * See {@link MicrosoftTranslatorClient} for MS translator configuration.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class MicrosoftTranslatorBackend implements TranslatorBackend {

    public static final String AZURE_ID = "AZURE_ID";
    public static final String AZURE_SECRET = "AZURE_SECRET";
    public static final String ATTRIBUTION_REF = "http://aka.ms/MicrosoftTranslatorAttribution";

    private String clientId;

    private String clientSecret;

    private MicrosoftTranslatorClient api;

    @SuppressWarnings("unused")
    public MicrosoftTranslatorBackend() {
    }

    @Inject
    public MicrosoftTranslatorBackend(@SystemProperty(AZURE_ID) String clientId,
        @SystemProperty(AZURE_SECRET) String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public void onInit(@Observes @Initialized(ApplicationScoped.class) Object init)
        throws ZanataMTException {
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
            throw new ZanataMTException(
                "Missing environment variables of AZURE_ID and AZURE_SECRET");
        }
        api = new MicrosoftTranslatorClient(clientId, clientSecret);
    }

    @Override
    public AugmentedTranslation translate(String content, Locale srcLocale,
            Locale targetLocale, MediaType mediaType) throws ZanataMTException {
        return translate(Lists.newArrayList(content), srcLocale, targetLocale,
                mediaType).get(0);
    }

    @Override
    public List<AugmentedTranslation> translate(List<String> contents, Locale srcLocale,
        Locale targetLocale, MediaType mediaType) throws ZanataMTException {
        try {
            MSTranslateArrayReq req = new MSTranslateArrayReq();
            req.setSrcLanguage(srcLocale.getLocaleId());
            req.setTransLanguage(targetLocale.getLocaleId());
            for (String content: contents) {
                req.getTexts().add(new MSString(content));
            }
            MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
            options.setContentType(mediaType.toString());
            req.setOptions(options);

            String rawResponse = api.requestTranslations(req);
            MSTranslateArrayResp resp =
                    DTOUtil.toObject(rawResponse, MSTranslateArrayResp.class);
            return resp.getResponse().stream().map(
                    res -> new AugmentedTranslation(res.getTranslatedText().getValue(),
                            DTOUtil.toXML(res)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ZanataMTException("Unable to get translations from MS API", e);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    //200X41
    public String getAttributionSmall() {
        StringBuilder sb = new StringBuilder();

        sb.append("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAAApCAYAAABwQGa5AAAA");
        sb.append("BmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAC4jAAAuIwF4pT92AAAAB3RJTUUH4QEfBS8nlQ");
        sb.append("tDEwAAC5tJREFUeNrtm3mQVMUdxz+zu9wgKghIKE8Uj2g0PxM1ZVleSIymoohoaRTRlKgV");
        sb.append("o7FRBBIt1CBG22gpHpjDA0w0HoWAuiZBMZpSsUGDHCEgYiDKucriwrq7M/ljv896TGZmdw");
        sb.append("HZJelv1dTse92vX/evf9/f1bMQEREREREREREREREREREREREREREREREREREREREREdFm");
        sb.append("seHM/bb7mDe9HOW6tchEEbRJkhwDTAE+L9KlS7f17S8fN3bRD3IZTgCyRfZ2LTBy3KmEKN");
        sb.append("WtQ0UUQZtEN6ApV9JdfQ4t0WcN0PX/SXDOObz3W1wDW9xrCcqiLrZJ5JrZJ9tEn2z+WInC");
        sb.append("lLpXqE9Tz7QFYiTkcM51dM71dc519d5vNTl2Sg9iZoQQil5/1e80s0wIIdea89kWSIF6Av");
        sb.append("2AcmC5935tgT4VwACgM/AxsALoA3wHmOW9X5dvrdvI2gYCl8i7PgP8sph32e4EMbNyYO8i");
        sb.append("uUsG+AxYm1ag7Y0QAmaWAa4Cng8hfNjEnL9U6K1VXr1zELBrCOHJAm29gdOAGSGENTuBnR");
        sb.append("kE3Ah0BGYCw9PKI+8wAHhZOjLRe3+zc248cLFkP7ENkqM/MBE4AJgB1Gk9hwFLvPebvuoQ");
        sb.append("aw9gAbAE+CcwF5gPLNb1zRL6jiguXA7s1QzlzpnZ3WY2dBst+8nA2UXaegMXSD47S46zt+");
        sb.append("R3lnOuVx45yoBhQF+gF7C7cy4DvAF8qL1uixgI7As85b0/A7jfOTcJ+Duw+1ceYoUQPkkT");
        sb.append("wMw2AqNCCPekLbaZtdflFxJ2hZQ6C9SnLHpGbfVAO/WpCyFkE4uv9vIkpg4h1KXmninSry");
        sb.append("GEUJ+a+lEKEzCzCs0BM0vPrT6E0JAOlTSnshBCrcYuL0HYCqDMzNppzfUhhIbUOPUia+J1");
        sb.append("yiXThlZSpjJguUKt4cDtKUvcHrgS2Ah0Sq3xt/qkY/+yRE66VS/LnehAg9pz3vtaEa1dek");
        sb.append("+BOu99NkXQ9qn2erUXatviWZGgAlip6/KU0eronGsH1Hvvcy0R0rZa8nZSqIPMbAxwJvAQ");
        sb.append("8CCwG3Curp/U94lSGoBDgEnA8cAjwFTgglR7P+Am4A/AE8AvzKx3QqAUegE/Ax7Xe8ab2Z");
        sb.append("6a10BZy9PN7C55nnIz6wJcAUzW+KPMrFdqzMOBXwNPmdmPZXWbwinAA8AfgWvMrIO81njg");
        sb.append("uCQkE64BhpaQa3NkX7YN+9sOeEpKNFiKnuB85R6vScET43aNooZBUubOivfnAjVAtWL+DP");
        sb.append("A75QDXA4sUrgF8H3hV5KsB/gYMUc4D8E3gBbVXA7dpLkhPpiuUrwHeAy51zu3inLsJSCoH");
        sb.append("Fzrn5gGzgZN07wW9d0BrJem9pXwLJZz1sj7dgGnAKuBo4E7gUgm1n2LaOuBpufxHgblmtk");
        sb.append("ALPhC4R9WY/VS2XJX37i4a40H1G07jOcJJwAcS9HJtxnptthcJ7tRmXAXsYWajgQ7A74EA");
        sb.append("3C3F/55CjEJoAPaX4jyg+YzTxt6i69sSkpjZ14CRwDkFNyVbXyOPV+wcpCvZzMYsrMzBMr");
        sb.append("2/EDlWA6Xi7vlApRLvQcCLun89sBR4TutOG6xvpMKV8cDVCrkmAruo/AxwMHAkYMD7wAol");
        sb.append("0FOV8D+sfmfJqF3knHtSpNpH+9IZ+BTIOeeOA16RTB4HNotskzSvpdKL3bTH86Xf3TWvJZ");
        sb.append("JHbWsRJLHqV4UQFqfuTzKzA7XopVrgsSJIVop9QwhhvZRnmGLJBUBPGg+75oQQ1uYVC9Kh");
        sb.append("3zJ5l6MlkBnARDPbN4Sw1MxWN3YLT+v5w4DB2vz3NY8G4Fci4Aki/GUhhM3AK2Z2YgnLnt");
        sb.append("E6JoQQZukdWWCsmU0UWVaa2cEhhIVSin8AswsVDh7uP3QpMEbhRUHr/8XGnguOYvRmyL1W");
        sb.append("oiy8SYahGDqJwK8DpwIvOudOlJW9l0by5RsCgFolvldI6c7w3i+WV+mXhJjqe4/Gai8DVQ");
        sb.append("381Hv/tPpPlXU/B/hICv45MNZ7/4VzroNCs7u1T2O99/fq2ceAN4HzgG8rkrgDmOa9H+mc");
        sb.append("K5eXHAyM8N6vaGl5ensSpEyxX1Uqzu4uAR0khajXIjqkhZ6QQ1gPdA0hZM3sPsXGC83sY+");
        sb.append("ChEMLEfIUws0Pkeaok5Kzyn+55OUKCffX9G42V05z20rOHAStDCJtTCjxblqrY2lcA61L3");
        sb.append("3pfn6BNCWGBmU2WZh8vyPZFUWfJxtd24PzChhAfpRkO3i7KMGZLJ5U4uch6SeJB/6bsYsY");
        sb.append("OM0UDnXA/NERmLYoeQdSpaVACVCTmUw6yQYmbkBWd672uccw3yKrMVuiWYBfwb6C8PsFKh");
        sb.append("97vOuSmAV95yuPb32dS73nLOfQDsquebOhdKcqxml3u350FhEqdmUhZxoJRqCPAj4Frg3S");
        sb.append("RvyT9rKIC3Fd58XSHRaDMbkc5B5E1GAG8B31WYdy3/fYKcJlWtrOHJwDH6HAn0EFmrk7g3");
        sb.append("tZZdSljqnIoX7fKsc1KoQJZ6sJmdKuK+EULIFqys5Ro6kWvoTa5hvyKfPcjSJdt4LrGPQs");
        sb.append("/8zz7KvTo2oTCbgftFhmHAt3TOsazQPqXW2136U5VKrvNRmzICnfKS+OSZjMKlco17HPCY");
        sb.append("yHSr8sC+6pdT3/T7NjUzH9uCJDsqSW8KnVLKmNFB06BCljOlKJm83KKDvMpkJXoH5PUrU5");
        sb.append("5TnSLO8DxrshHoaWYVqjIFkeDclAcpS3m2SuUjx6oq1xU4o0isn4SXBwBHmFkHM+ussRcl");
        sb.append("IU4I4V1gnnKUBcqN2gpm0vizlFuAHklFqwTKVdpPPMmXltk516GIUn4qXeirvDJR1N1F6I");
        sb.append("+0Jxu898PkERapfJ5TLtJJeWzyvi4i9nrJuZQ+txexWvT7w20NsdalksA6VRfSivkOcJnC");
        sb.append("hWWy1EsSKyDrui5vzM+AGjPrqNi9p3KXHvIkI9SvSiXhOjN7XpWr9hLSALZMXp8ARim+XR");
        sb.append("xCuMvMnLzSkRLunkCFmV0XQphnZlOA+1Q1OULKvbEEQear4NBfcz0BuFLzSwzAvaqY3RBC");
        sb.append("2NSKp+6ZvO/lKqRcAqz03r9YpF+igB1VVLkRONQ596iS5V7ao1vVN19h7wTGAuOcc146c7");
        sb.append("PanpOXvtg595q813pgg+R7h4g7Qd7jU70f4GXv/epUJSwjEjU455Iw9WLn3AvSpTU7yoNc");
        sb.append("KWuLcozbZAWSU+uFqli9I4UeBfwQ+LOeWaBEL1+I05UcP6wYdbP6nhdCmKvzkzGyYgDPay");
        sb.append("6fKPk/Exgtq0QIYYpCvFeB98ysIoQwjcbT79mybLP17mqN+ROVLD/Xhl+hMnUhrFTF7RId");
        sb.append("pC0ChoYQZuokP+m3SgZiFkAul2stj1Ej5auRItXIEKzTHibYrH3boOtqKVet975OBuEvwI");
        sb.append("VK9J9VNQ8VV9bkFRpuF5GOUTTwirzHOIVSdTKof9W4+4tQVdqbu0TCGaoomozY2FSksDo1");
        sb.append("X0TkD4Cfq0rXZ4d4EBFgeurvtVpYfp/FKUXOb1sDTEsl9YQQ3ki1L5Ky5T+XS0im66yU/9");
        sb.append("VU15dS7YQQ3lZOkx5nqSxKoUPRBpUfC807//qz1NhL8+eq85WD5UmfCSGsBpgzZ05rEeQl");
        sb.append("7Uk6zPuTDEZa3m8CpyeHrDqrqhTJkyT5fIU5XUW4RQqJRkq/liU5g/e+2jl3tQ4be8ozfA");
        sb.append("zMk7X/SBW+Pqmiz3yREefcdSrf95GXWAW8l7SLoO+k5os84wpFCBuTuUe0IZjZaWY208zu");
        sb.append("1wFlsaJEIyZXncLkqlzJzyO5s+sry6bnKsmV+HySq+T49NCFfrnbnHv5aE6fYu9saZ+m3t");
        sb.append("XSNbSk1Bv/YWrHEKRQMaI0QRotenHU7zqkfs/y4eVkTy/RaxUwNDNoi7JqRCudg0QUwVYk");
        sb.append("4rWKu4ufpDcWR9bqDKHYfxS2+OQ4IhJkZ8DrNP7ILlfU82fI5ih7qRlnY7kozoj/HUyu2u");
        sb.append("5D5iqjWCMiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiKajf8AVIDx/3sYafwAAAAASUVO");
        sb.append("RK5CYII=");

        return generateAttribution(sb.toString());
    }

    // 400X81
    public String getAttributionMedium() {
        StringBuilder sb = new StringBuilder();

        sb.append("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAZAAAABRCAYAAAD8fPBpAAAA");
        sb.append("BGdBTUEAALGPC/xhBQAAAAlwSFlzAAAuIwAALiMBeKU/dgAAFQBJREFUeNrtnXmYHUW1wH");
        sb.append("83KwlZCGERZEvAgBBIMmciKpsKRgREDQqoqIAiiOJWQPTJAzQoshQiKqAiiKCifiCoiQ9E");
        sb.append("QYGwJDWTGBaTQBLAR0jCEhLINsxc/+jTTE1z+y5z72QZzu/75rvT3VXV1d3V59Q5daoaDM");
        sb.append("MwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMw");
        sb.append("DMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwNgUKdg");
        sb.append("sMozpWfmj0mcDJQEcdxWw57NaFo867g9HAHcDaOsrqB0z51iRus6djbAz62S0wjKoZCby1");
        sb.append("QWUNAHZvQDlD7bEYG4s+dgsMY7PGvAiGKRDDMAzDFIhhGIZhCsQwDMPYVHDOjXTObbep1M");
        sb.append("cG0Q3DMDZtpbEFcBXwEWAQUHDOFYB9vfePmAIxDGNDCKLDgL0yu9d4739eYzl9gFOA/tHu");
        sb.append("/sAt3vsno3RTgHOBZ4DjvffBnkLNz2w0sIDS3qItzAJ5gyEibwZ2A14JIczupdf4dpIw1T");
        sb.append("khhJdqyDcAmEgSWTQzhLDOWkxDOQn4eAkhda/3fl4N5RwCXF1i/wLgSS3zw8D3dP8ewN+d");
        sb.append("cyO89x32GGriAfKHGjZ6BF6/DSBMzq8j+z0hhL/1sgbxceBiYA4wvpc2+nu0bR0K/L2GfC");
        sb.append("OBe/X/0cAikx8N5dWc/VNIJkhWy1dy9sfKYc/MsaHANsAyewxVWx/HAdtGuxYB7/DeL3XO");
        sb.append("7Q6sjtIeC3wSmO69v6o3WSDn1ZH3QqC3KZD0JWvrAWX9ANAOTAohvLIRr3Gdtq16eptFEy");
        sb.append("Eb1DKpSoE45wYDR1eR9KfABUBf3b7Ve2/Kozbem9n+iPd+KYD3/gl9HpOBm6M0f+lVFgjw");
        sb.append("4xwh2gy8QwXFNcD6EuketDZUE/vrb1+7FUYF1hL50J1zX/PeX1ZFvm9mOgoDSyXy3r/gnB");
        sb.append("umyuZZ7/3ddstr5s2Z7YUl0ozYmBXscQUSQvhiTm/5bFUg7cCUEMKL1l4MY4OxBBgObK3b");
        sb.append("XwSqUSBTMh28g/MSeu9XAzfZrd6kO/ibbQXTgaECVc5HEZFtgDcBa0MIj5c4PgLYXq/r+R");
        sb.append("DCku5UTERGAVsCy0IIy6rMsx2Jj7cArAwhPF3PzRGRgcAu2ktcGUJ4MiddIYRQzCjtlTWe");
        sb.append("a7Re70shhKdqyLcVsBOwJoTwRE81FBHZGdhKn+kzdZb1uvv1BmU3krG4VCGMcs6N997nBn");
        sb.append("Y4547PWLe35SkQ51w/oEk7iAArUrdLTvrtgHcB++l7tAaYBzzkvW+J0o2Oet0FYK73fp1z");
        sb.append("bg/gcGAw8FfvfWuJc7ybJEhjZ5KoseVAK3Cn977iO+OcOwA4ENiVZDxpITALuN97314m30");
        sb.append("TgbSTBBIOBFcDDwD3e+8WZtMNJxo/WAkMyRe3rnFud2bdzZntH51yzenb6AnO89z0WjLLJ");
        sb.append("ajgRWaU3ez81k28C3hIlKWi6PfQlmKwNqxCV8SLw3RDCpTnn+ChwA7AwhLC3iJwIXK49sz");
        sb.append("TNY8ARIYTFOWWcDHwX2C5z7leBucBpIYSHqrzmYcD/AMeq8ugbHVsHXB9CODWjLIoi0gKM");
        sb.append("idK+TNcxhLtDCB/InGsISWz5cUThmHrfLw4hXFCmnlsDf1ILMn0Oq4FzQgjfpzHRIe0ich");
        sb.append("hwo3YK0nMvBo4NIczM1Olq4BP6LMeVsYiLIrJCr3lKCOFHb1AFUiCJkootii+QhOfm8eXo");
        sb.append("/+nqwspjK7q6oKcDR5YQriOAX6vwzxPAP/Tef0k3LwA+Fh3exTl3BnBWtG+ctoU0/znAOX");
        sb.append("nuNk1znff+5JxjbyEJBtkpJ/tlgCuR7wjgOpUNeef9J3C09z6NVjxI361S/LOK5/pNuroZ");
        sb.append("x5BEx/WoFbApsqXW751AUOWxHHgWWN/c3JzW/Vjgsyr0l2iPoFW1/AjgEhG5qYwCHQgURO");
        sb.append("Q8fdhb6DmWa5q9gAUiMryEIL0E+LkKuJXAv/SlWUQypjOBznGJathXX+hRwAvAbGCm1mcg");
        sb.append("8DkReUZEss+tna6D8q9m/toz9R6j5Z+ggvRJrfdTJNEyU0Xkzhzlsb3e51R5LNI6tgGXic");
        sb.append("jXKwiWajkG+Ks+w9nAo6oUdwMeEpFJmfSXaI9tPxHZp4zyO0bbyuA3sPJIXUwr9H1J+WwZ");
        sb.append("QbcL8PZo18WR+6sa2kqUOU7fs8Mr5B1YppxjM8qjSxrn3D3A1HLKQznJOfeUWk5xHfcE5p");
        sb.append("dRHnn361JgWjnloRwMrFALCuoLPClFj1rbm8M8kKuB+4CPp+4VEdmqre21NvI0SQTJH0MI");
        sb.append("z5ewMH4HHCciN4QQpuWcYxRwvvZSLk8jmESkGfiHWkJXEcXQqyA9M20vIYQu/uOmpqaBhU");
        sb.append("LhgBqv9RXgIuDKrCtJRJpIvh+xA0nQwclRr3qipkkby44hhNU5AnSQWkb9VTB/MD6XiEwE");
        sb.append("7gQOFZHzQwjnZ4q4X/OuAg4OIcyJ8l5AEjm3vs5n3gF8X3utk9P5ICKyLTCDZBn0v4jIoB");
        sb.append("DCer0HT6gl1qQC5cScsv9Xf6/vJXqgXgFxBfDLSPCd5r0vNcfjjOj/Jd77fzjnDu7uSVUh");
        sb.append("zS5R/1vVdTUQEO2R511jO/DtaPt5klDw/nqO36vLKeYu7ZCi7qxDMu6g+3V/yq8z+acBf9");
        sb.append("D6NWsnrJC5ti+XsEjmA7frO74n8OHM8VnOuZHquluoLqxdtSMdl9FewtLbIdp+BkitmUH0");
        sb.append("QLTn5qZAVs6dO/eg9evXFyOBuSL6/4Yy7orfi8hZ2jv9uj78vB6ODyF8J5N/loh8hSQk8W");
        sb.append("N0nYSVzuFYkVUeAC0tLeuobQ4EOrFwds6xFhE5OlWmlA+77FdBYAwgGU+YUOI8M0XkcBXU");
        sb.append("56liTRXEkapsUcE+J5P3HBE5QH3Z9VrGS0MIR2bKXy4iY/XlKqg74+woyQ/VijxWRE7NTk");
        sb.append("RUyyt1b3XH+nhCraJ6eomD9Xc1cHed1lpftU7r4VfAtVGb+SalJwnGAvFnDXiv/xi50gAW");
        sb.append("AxPUKoqF8Sh1s+Zd/2B9JoenkxSdcwc45w4hWfojVrQHeu9nZMo/VDtMKc3OuSO999M0iq");
        sb.append("wpOnav9/6oTB1O1nOl5Q3Uzk8X15/3/orMebdXRZl6NoYD3/DeX6AdJJxztwOxpT3ee78m");
        sb.append("U84n4w4AcJ73/poNJZw3BwVyRaw8usEvVIEcWCHd2Tn7p0UCaGRk5aRfkttKRHbvyUHkSI");
        sb.append("DO0LGGwSLy1hDCY90oJvUNX1TmPPeLyH+AnUTkqBDCn/XQ8WkPtMwEz/PUaquXS3Lqtk5E");
        sb.append("riOZu/DZzHO7XoXbIHWBZXuP6fjRohDCrForNOzWhdeo9Vc335rEU8C7NwE3Vodz7mqSKC");
        sb.append("yAnZxz+3vvH4yE1Gcyvey6XH/OufGRIkeV6Fu996W+zrjYe/+9MsU96b2flLmm+5xzd2fS");
        sb.append("nZlVHpr2b865czOWzKn63vcrYfGUuodxe/9C5l7dmFUemmepKq9ZGSV9QaYjle3orsnsG1");
        sb.append("Bh+w2vQO6rJpGInK7aegxdv9JWzZyIJSGEvF5l/MCGqZlMCOEfIpLG0s8TkV+RDH5Nz3Mf");
        sb.append("VYuITAY+qC/Z1pkGOUh/R3aj3D2j/K+qRRH3AtPf9cDLkXsvZe+0J1ZG+fxTRBrx3O+qcO");
        sb.append("wkYISI9A8htOm5iyLyU+B0ktnSWQVykv52b6bujS++T62reiyQgZww4szi7Wyrrra2Ot/f");
        sb.append("3xTeR71L4nwrUiCQDJbH1vY50f+3e++X13m+yZntKTnKA+99pc7jd0soqAJdx2soN8fFez");
        sb.append("/VORcrEImsxDXRO3NIqmy993mz+o/JbF9c5rzBOfcEnV+m3Mo5t7X3/oXNxX+6OSiQZRWE");
        sb.append("4t4kg+YDMj2FYom0rwmbMkqiHK8J8ubm5kKxWBwPPKSK5VP6h4gsVTfARSGEastGRIaSLH");
        sb.append("EyKudailEdujNZcJvo/8uqzDO0RP5KIcq5E8xqYEWZY/+J/n9TXJ+Ojo5z+/TpczowUUR2");
        sb.append("icbNjqAz/POybtbp3XSNWuouZ+q9PKsBZT0M9SkQ7/1zzrk7InfJMc65Qd77NRoSuluUfG");
        sb.append("oD6pxdwue3dZQ1s8S+0Zn2968qylkUvXc7OucGeu/XOuf+RDJQH1snpzrnfqbuouxUgXdE");
        sb.append("/6+mcgTUXLp+2nhchc7TJkVv+B7IbFUeM4DDSGZvDg4h9A8h9CcJh61aMVTLrFmzisD8EM");
        sb.append("JwEl/r9UC6tPL26spZJSJvq6HYadqIl5H4fXdXAT5Ar2cAySBcI573D7SO5f7O76Y7qhFh");
        sb.append("vNWW0aWj0Nra+jxJsAEkK8GmfCO1I0II7d2sUyMjWja1uSjfy7hBPhq5ZGJ30X0NONcOGQ");
        sb.append("VWzzjOqhL7hmW2q5nb9FxmO+2gnQiUchWfAjzjnLusTLtdm2dZRWStueGbk/DdrFfjFZHP");
        sb.append("k0RcLA0h5EU8bd9T508npIUQbkbXoxGR/mqi/1jdTLdQRQigTso7SDf3CSE8VyLNMOrzcc");
        sb.append("ZRan8IIdSqHJapQt6tQrpG+GG3Jhm0LsWur5ln7e1LcoThJOAzIvI57VQcCFAsFqdilLJC");
        sb.append("7nLOLYmE+5dIBmc/mul0NIK+GZdTnwav0psta3AVebYoVYYOWu/tnDudJAAla/l/1Tm3nf");
        sb.append("f+hErXmcOgKhSiWSA9RBo7fXeZNEdsyAqFENpCCL+lcyG0N4vIDlVk3U9/15RSHpHp37+7");
        sb.append("vfcQwqN0uuuO6cblzdXfg8oowvc06FYeVsWx5bNnz24vcZ13kcxVgcStmLqKHm5paZlv6i");
        sb.append("KXn8SPUpdk3zKymH7RoPNklf6EBl/H89lmWUWevTMdpXUZBXul974f8Bng/zN5P6GTDbMK");
        sb.append("YJhGW5XjLZntej8QtUEt281dgazJKJKsMBtEFIa6gXmpxof6ck6PJKZa3305i+dG/T1DRN");
        sb.append("7U3NxcqGAZxcfTCZnbishROVkubND9O7tUXURkJJ2RZFdVIQwdnQPCPzYdUZashRFPwJ3m");
        sb.append("vW/UenXZD0ud1ciL8N7/h2Ri72tuIefcvnnpdZwnthZa8wbvvffXeu93IgkdjklDee/IdO");
        sb.append("TKzbAfRDKXJGVtne48quxgmgJRpnfKFnEZYbOrukB6ZGVaETlNRG4uNcahM8VT4bYohFCx");
        sb.append("UYQQWqL8f8mU119E7tCeVDll9Kj+XtLU1JQXpXUayeBeEXiqWCy+zmIYO3ZsXxE5UURWxO");
        sb.append("tGhRDuAP6tm7fo5Ma4nleSrPlT7+SlDpLw6JkiErtZt416aOtDCLmfCigWi6myHUviSiwW");
        sb.append("i8UbTEeUFbwr6JyfAV1dkZc28FS/ymwf55z7UBlB29SNc/wms/3LKjpVKdfqebd0zu2ak+");
        sb.append("d3ObL02sz+K3RuSCkuysiny7v5rsTsvCHbzMYcAylW6J2nEUfFMkJ3hojcCnwIuFREziWJ");
        sb.append("Stk2Mg2PAv7czTpUYjIwWcNWH1GrY5gKLUgiqI6uobwzSCbDHa6zyh8kiSYZHx2fSjL7tB");
        sb.append("RT9cV5f6FQeC4Kp70vhHBgdK27k0R7bQfcKSKvqPJpB3akfODBwSQRK1sCQUSeUJO/WXs/");
        sb.append("XyUZvK5nmek+JNEuPwHaRGSWlj0uelaHliugpaVlVdQ2AG5uaWnZmN9I2Vw4v0SbfSYz16");
        sb.append("FeRTXPOTedru7lP2hk0zXq4hoEHEAS9TaD2j54lVo18bpx451zj5FEwD2i7WhPwNP141cL");
        sb.append("vfe/ixToYg3dvZ5kMH01yZhadpXxVr226c65xyOvyFBgqXPuqyQfWlun79iZdJ3o2EbXOS");
        sb.append("DVsjgrQzRybKF25mY0wKrZJC2Q51XYP0LpCTqPkMzUXFeh5/5hkgUI16nwfqcqjz+rNn5Q");
        sb.append("LZEFJRTFSj1PuVC7dhWu/6brEh23qcBOF0rcR889liRS6kpgmxDCw5nyXiAJK1xY4lp+RD");
        sb.append("I2ka68u78qj5nAu/T4Aq3L6hL5b1JBfjXJXI05+vdolKYYQng2hLA9yVyJf6symEgSO78L");
        sb.append("8Li6y8aWOMdyVTxpj253ktDFZ4HjQwiXa/6SdaxAm7aJx7Qn3KzPr5nOiWd3FYvFMSGEe6");
        sb.append("so79KMcjUqC/fWuL0o3+6BU32A18+iP4XONdnmaW9+z+5YtN77VcD7M7v3UrmwSAXv7Zk2");
        sb.append("/krGpRRb7feTREy9pPnHRR2yWd77OJz4PRk31nC9lgV6bQ9klAfAe733r3TjOrMhvwNJIi");
        sb.append("efJgnsGdorLZAQQtlZvSGE/Woo60LgwnHjxvXv06fPgNbW1uyD2CMn3/TIDZZX9kpVDtn9");
        sb.append("S7SnfS7AmDFj+gwZMmRIS0vLygrlXUey3Ebe8VtIIreYMGHCsNbW1pWZ42+rUH7g9T7mvL");
        sb.append("Q/QP3eTU1NQzo6Otpnz569pop8q4FPA59uamoa2tLSsqqWOpYp9zmSBSVTnlWlhogMq3WZ");
        sb.append("ejojiOaFEP6FUS2Xkyzfk7pI8t7TWsK1CxnB16HrYc3IEdox3XJDe+//zzl3JMnaVZUiAx");
        sb.append("8H3p4Z58leX39eP8Ywj8yCqd77p9X19QCZkOUSvAy8J6OAauVTFVx0vdKF1XDmzJnTRg8v");
        sb.append("HpbH/PnzO+g6cFc3WeXRk7S0tLzczXwbJOywG8oDOpcf/47pBFCBNlB7zeVCW2+KetGhzL");
        sb.append("cuHiaZBFhUwRp/q2UdycKI6TyIWSUEfBsw0Tk3iSS6aX+SAJBC5KG4ja7jCg9GyqBAhbBX");
        sb.append("dSkNAz5PsoDhPiTu1XatbwB+6b2/rUT2FSQBG5PV4thVldlyteyv997/Oue8T5FMSPwYyY");
        sb.append("KL4+mcUvCceiF+D1xb5v7eTef8lAI5i5R6729wzi0nWTFgvD7bZXpty3uyQRXsnTJ6I7qk");
        sb.append("ySnAyo6Ojq1bW1vb6y70xhcvJFmUsz5OGFEo3s5elJ6gVnPvs/A+LDjA2Cj0s1tg9CKl8T");
        sb.append("USf/UOdH7NzTVEeRiG8Tr62C0wehG7kARQDFHT/ws61mYYhlkghpFPCOErJJFlhmGYBWIY");
        sb.append("hmGYAjEMwzB6FebCMozqaQdepb4PSqUhqOmk1nq+H9+/zroYhikQw9hATKXMp4CrJA2dX0");
        sb.append("AyQ7ne1VPX2mMxDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMw");
        sb.append("DMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDMMwDM");
        sb.append("MwDKPX8F8Oaf0WVY4ArgAAAABJRU5ErkJggg==");
        return generateAttribution(sb.toString());
    }

    //800X162
    public String getAttribution() {
        StringBuilder sb = new StringBuilder();

        sb.append("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAyAAAACiCAYAAAC54KkmAAAA");
        sb.append("BGdBTUEAALGPC/xhBQAAAAlwSFlzAAAuIwAALiMBeKU/dgAAIABJREFUeNrt3XmYXEW9//");
        sb.append("H36ckkJIEkbIFAWFQEFIGkC7l4FS+K4oYIiAtXURT1yiZouQtycUFZSsHlgoqIInpFVBBE");
        sb.append("NkV+oMhSPVnYgoQ9bAECWUgyS5/fH1V909PTfXqZnpme5PN6nnkg0z2nz9Z16lvLt0BERE");
        sb.append("RERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERE");
        sb.append("RERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERE");
        sb.append("RERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERE");
        sb.append("RERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERE");
        sb.append("RERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERE");
        sb.append("RMZYolMgIiLttPzgl+aAiR30jFkz7bIHUoBTrqUr7lsnGDj1AHp1x4jIhmaCToGIiLTZbO");
        sb.append("BjwEwgHetKPvAV4IX4732AD3bAOcoBd55yLeedegB9umVERAGIiIhI62YCRwLbdcj+nFYW");
        sb.append("gLwK+GSH7NcNwPmgAERENiw5nQIREWmzIrC2Q/alj8G9MAOMfa9MiYZfiYgCEBEREREREQ");
        sb.append("UgIiIiIiKiAEREREREREQBiIiIiIiIKAAREREREREFICIiIiIiIgpAREREREREAYiIiIiI");
        sb.append("iEhLtBK6iIiIiEibWGu3AbYBNgG6gBeBZ4H7nXMD1trEOZduyOdIAYiIiIiIyPCCjqnAfw");
        sb.append("HviMHHFGAikAD9wBrgBWvt15xzl2/o50sBiIiIiIhI68HHR4AzgRmEHo8suwIKQHTbiIiI");
        sb.append("rFeVoS5gowbeWmqZXTvSw0GstUncpy4grbNPfc65tRnbmgTsB3wK2Bl4Evg9cJ5zbrXuAB");
        sb.append("nF79pE4GLgMJ0NBSAiIiIbsn2BUwmtsfXqALcBnwGWjfA+7Qh8F3hpnQBkInCxtfbMakGI");
        sb.append("tbYbOB04oezXOwGvA46w1h7snHtEt4CMQvAxBfg1cJDOhgIQkUGMMYn3PtWZEJENyAxgDj");
        sb.append("Ctgfe+EjgPuHWE9+mAWFFLGnjv9tTO0nlcRfBRbi5wurX2Q865Pt0GMoLBRw74IvBOnQ0F");
        sb.append("ICJDeO9TY8yXgKnxV93AX4C/eO8HdIbGTSB5LLAtUIzX8Ebv/VUj/Jk7Ae8lTCQE6AV+57");
        sb.append("2/S1dEOlwRaKYCfoS19raRGoZlrd0UeEuDwQdAVtl8fJ2/3RvYHSjoNpARtCfw8Qbv6ReA");
        sb.append("yYTePWhiCYz1OVuWAhDZEBwHzCy75/uAv9V5yEln+QhgCOPVJxDGkV81wp+5I2GM+ebx3y");
        sb.append("uBu+KPyPrkE8DngJGaP/FS4G1tCGQmUr9XZ1rZd1ZkJALqicDBwNZ13vpN4Mfx2QEhM9b7");
        sb.append("gefqbP+VwAeAPZ1zB66v53HcByDGmLGMDDf23q/S13Fc3Ofl93rXOLivB7WQeO+LuoaD/j");
        sb.append("sa1zBXce9MoPEWXJHxpBs4Ejh3BCpr3cAhNDYpPpNzrtdau6pOgLEceF6XVEbQJsC76rzn");
        sb.append("QOfcnyp+9xxwUsX3oys+a3YCjoiBx/bx5UXr80nUSuginRd8TAVOBD4Zf441xszUmRGREW");
        sb.append("RHsLL20TZu7yd1Xi8AC3Q5ZYQDkD0zXv8lcF2D2/o8cB9wN/ClsuBjvacARKTzbAY44Ifx");
        sb.append("53uEIQwiIiPlZdba/Udgu28GZrVxe6cBv6jx2g3AqVkpfEXaoN7z+DLnXG+D2/oAYbjvBk");
        sb.append("cByPBoOIaMhGrDrTRfRURG2udHYJuntHNjzrkiYbXp9wE/Aq4ktDj/F/Bh59yduowywrIC");
        sb.append("6tXAE01sa82GehLXh0noL2+yYrcjoUV5t7LfP0lI63dbE+ckB7yo76GIiIwjD1C7BTdvrX");
        sb.append("2Fc+6ednyQtXZf4BVVXkqBR4AdWgxC1gCXWGuvACYB/c65lbq0Mko2qROA9OsUbQABiPf+");
        sb.append("/mbeHyf3VnbP9gNLvPcP6ZYQEZH12L8ILbiTq7w2A/gg8JU2fdYXa/y+CPhWA5CyQGQ1I5");
        sb.append("e5S6SWrNEvWnesQRqCJSIisuHYEri6xmsTgP2ttTOG+yHW2h2AN9V4eR7NrVMiIusZrQMi");
        sb.append("IiKy4dgU+AYhNW41uwL7AZcN83OOp3a67F/S4StIW2s3B7YgDLeZTGiw7Sf0uCwDlo7UsC");
        sb.append("9r7XRCMpIuwoiNpXHYWTPbyAFbxeu9MWGoWlcM/NYQ0hUvdc49P4LncBYhZXLp84tln/2M");
        sb.append("c+7ZEThvWwDTCWmfJ8bPXAusIqTBfaINC/vVC56buVbFFl9TACJgjEm892nF715BmJ+yZf");
        sb.append("wi9AKPAdd77/sa2OYuhO7pLWMBWCqIlgGPFovFBT09PWvauc8Z751FWFl2W8Kq0L3As8B9");
        sb.append("3vs723geuwlzc3aIhe9kQlfnWsJCPkuBJQMDA/+aN2/ewBhd6y2Bl8VzsWksVAfi/j0F3O");
        sb.append("+9f3CY16LaA2H5CB7TzoQc5DMJK8an8fOWAAu998+MwGfuCLyKMBRkYiywnwTubvb8dcD3");
        sb.append("fyPCIok7xgcf8Xt6v/f+dpWQ0mE2Bq4BVlB9LPt0Qi/IVU1k8qmsCG5GWPm8WgCyFriIsL");
        sb.append("hoK9venpA5qFijYnidc25hi9veMe63AV4Sy8Tp8blXqryvis+/J621i4FbgD9WCxCstbMJ");
        sb.append("66tUe+b3Atc45+4ue/8c4D/j83bz+JlrgH9Za89qZIK9tXYvYP+4jdlldYjJcXu9MYh6vu");
        sb.append("wY/gH82Tm3rA1BwJbAocA+hLlGMwmLQ24Un5WlAO5pa+2jwELgBufc/BY/byvgAGDv+Hlb");
        sb.append("ldUfygOQ5cAzwKPW2gXx3M+rs+2EsIBmvuIa7pPxZ5OBT1hrH6F+oqJ+wuKEtWxhrT2BoW");
        sb.append("voJPE5faVzbtwujKsApLmKxubAxbHSNBDP3+ne+4vL3nN0LFi3jpW5UqtDEXgY+I9Yka7c");
        sb.append("9iaElTXfEQuOjWOhN6nsOhXjl+DFXC630hhzG/BD7/0/mz0W731qjDkX+Pd4LJOAK4Cveu");
        sb.append("974z7tBnw1fvlmxC9BV7zx1wIrjTFLgO977381zMro54B9Y6V+Siw4usqOu79UcHZ1db1o");
        sb.append("jLkHuKpYLP62p6fnhZG87vl8fp8kSQ6KrYIz4/5NJizelSsrSNYAq4wxjwO/As733q9t8F");
        sb.append("p8FTgsbmdilbddboxZlVGgTSWMt76skcAy3m+fIrSCbllxr1LW0rfSGHNnvMbXt+E79G+E");
        sb.append("hZh2iw/20mcOxPO3whhzH3CG9/4vnVwe7LnnnlMmTJjwtfidnRHvi+6yitAqY8xS4Bfee9");
        sb.append("fg+XkL8H3WrZy7UfxOXjrM8+5ixapUoewG9mrk/pT1zkTn3IvW2nOpnfXqQOAc4P4WP+Od");
        sb.append("sQJfzQWEluhNWtz2SwipeKtN9C21cjcVgFhrdyJk63p9LA8n1/mT8kn8HwFOs9bu55x7rO");
        sb.append("J92wNfr7Gvy0uNLnEfTiWs+1RtzadXAb8D7sw4hjfFZ8BuhF6AZup3RwBPWGt/C3yz2d6W");
        sb.append("sn34bDyGbeqcw+0rAtIV1tpdm+kRicMET4nl79YN3E/blv3/e4ETrLW3A1/OqMQnMZg6qu");
        sb.append("IaZgUWUwgZ2RqR1rlOWwBn1vi8lNCorQBkA1FqoZ9d9ruZ8QH/ihic7EHtbufpla8ZYybH");
        sb.append("CscHyiq09aLm0vjclwPvMcZcAhznvW+2lXynuL8ldwI5Y0wSK6dn1KgMl75kmwLbAfsYYw");
        sb.append("4sFovH9PT0NNWda4z5UiygGznucrsAB+Vyuc8ShgyMRMB5cLw2W8XrVm/O1Cbx4bVjDOw+");
        sb.append("Y4z5kPf+7w183HYx8Mw63no2b/C4jiMMwdikzjFtEu/vlwBvNcb8Gfi4935pC+eyKz4svp");
        sb.append("zx/Zgaj2FH4I3GmO8BJ3nvO2mSaRKP5/WEISozaty3k2Or3yzgdGPMx4DDvPeZD4s0TW9K");
        sb.append("kmR2xcP7FODSYdzHmwCfqfj1FQo+Nlil798vMgKQHYF9rLWLmx2uYq3diNAiPbXGW74X6x");
        sb.append("6Th/MdrFF/mUATc1vjMKWTgc/G/W0ltf70+LNprBA2uq/dpdettT8DPpzx+f3UGI5jrZ0a");
        sb.append("r+W7MsrWeqbFn5OAd1trP+Scu6OJ8zgFuB54TQufPSn+dDfxeQcCPyf0dLRa950FHAS801");
        sb.append("p7inPu63W+LxOa3H67njfdGcHLuJ7HrUnow5caY3YFrgLm1ikAkirnfBKhZbLUu1BZAA3E");
        sb.append("wqefodkVkvj3RwCXGWNmN7vvNW7oM4CzM4KPag7P5XI/NMZMaaJidFFsyap23GnZcdc69q");
        sb.append("5mCq0W7BWDze4q1618/wYyWsluNsYc0sRDddiV41ry+fwWxphfxKBqep1jSiu2OzE+4K6L");
        sb.append("wXYzFeBu4Pz4oK/2/ShWecBOiJXm7xhjJtA5mUX6jDFvB26MFY6k4twN1HiA7RrP3eszL2");
        sb.append("CSrAX+p+LXrzLG7NVyAZWmx1X59RkqujdYG8f/LgH+kPG+jzN06Ecjdif09FdzTaykJxkB");
        sb.append("yqiI8xN+C/x3PCejva5XCqy11n6S0ACZtHAMuxOWDzh0GMFHpVcAN1tr3xGHINXbhy0Iq8");
        sb.append("+/ZhSuWc5a+23CaI3N2rTZBPiatfYKa+1kFQ+jRz0gwzcL+AmDV7J8CHicMMa2NLxpMwYP");
        sb.append("cakMMkqWxwL66fjzJGE4Ro7QOrwDoftyl4oC6w2Ay+fzRxYKhVZbjIvAqbE1qOQZQr72ZY");
        sb.append("Su0q5Y8XoZQ1vc/zMWhuc0UCk9hZDusbJAvg94NJ6DpYQhI93x/G1D6JLcmnVduCMZRFe2");
        sb.append("OD1KWGBoafzvc4ShNhsTundnE3qVtqj4u98bY97ovb8h47MeiIV4aQjWnIrX74r3QdYQrK");
        sb.append("czzvemwLmEYV7VPvuh+LM0Xoet4jXereI67wn8whhziPf+sQbP4zmEcdBpxf4vAe4BFhPG");
        sb.append("VG8Sz9/O8bMhdOc/SGfkVe8n9GwdXva7FYThHvfF7+oEQo/RzsArK77vs4Cf5PP5gwuFQt");
        sb.append("V1Frz3A8aYi4FPV9zbn46VlOafrklyfMWv7i4Wi/9Q0b1hc849b639A7Uno78+fg8bnudn");
        sb.append("re0iDKXdtsZbfkkYJtU9lvWPOI/kZ8Abx/ASDMTy9d200JBmrc0Dv45lTbtNIizweDBwec");
        sb.append("Y+bETofdllFK7ZRvEZduQIfcSBhLVlPuCcW64SQgHIePA+1uUyfxj4MaEr8h7v/YqyCuA2");
        sb.append("wG7FYnF5leh7Uqxk/YEwGWzBmjVrHrjrrrsGalQmdyH0mnyWMHSn5L1JkvwpFgiteG1Zxb");
        sb.append("4fuJAw7nSe9/7Jss/fDHg1YZzjIRUVy28bYy7y3j+XURneAfhExa+fJfS6/CFrmEo+n98s");
        sb.append("SZJdCL1Nbxjhgq/U+vcH4C9AT7yuy2oc14zYCvTBGIyV+6kxZg/vfa2sKd8jrOpbqqhWjm");
        sb.append("E+Ebg94zubACsy5n+4KsHHC8B3CMNxeqoczyRCS+YxhN6P0nXeKwYV724g0PwEcHTZPpb8");
        sb.append("CvjRk08+edOSJUsqEzjMAT4WfyYResme7YDv+kbxvp0W/10gDGW71nu/quIYto9lw0ll7w");
        sb.append("fYOUmSb+fz+UMLhUKtnrMHYwvfu8p+t78xZhvv/ePN7LAx5t0MXbX3nFxOnd8ChAnU82PD");
        sb.append("QjWfbbLCtwm1J5ffC9zmnEuttaUGp7EIPjYmTIJ/fZVGkVqeiYFTF2GY7aQ27EpXLJN3be");
        sb.append("EYtiT0KjcafKwgNCJCGDY6rcG/u8ha+2bn3K01Xj88I4grnduV8dn5fAy0toyNNJs1cbxd");
        sb.append("sSxt9F4sxgah/nittmoiCDnZWvtl51wf63r/O1FC+3q9FICMU6XgYwFwtPe+astirDhUqz");
        sb.append("ysJkzAvhVY7L2vm3bNe78IWGSMuS5WVF5W9vKZwwhAti/78h4J/LY0Ib3i858DrjHG3EqY");
        sb.append("OHx4RSXt48DpGZ/zNtbNY4HQy/FN7/136+1goVB4Lj40bzHG/IrsDBLDdSVwHXBHraCj4r");
        sb.append("w8D/zZGHMzsIjQm1R+nxxb67zEwGRlWcW/0jLvfUuT7Y0xH6lSKXgCONh7f1vG8awFrp07");
        sb.append("d+7tuVzua0D5UJ5DjTHv9t7/LuNztye03Fc6G/jvWsfjvZ9njDkhBh1fig+tmR0SgJSC0t");
        sb.append("uBQ2v1AnnvHwHOzOfz85MkubqiknNQkiSHEoZ/VL2PjDF/rAhANo2BbbNDp06o+PeTwN8a");
        sb.append("KWdk/eecu99ae1NGAPJBa+3xzrkVDW5yb8KE6WquIyyCONZ+HIMP6gQf9xImzP+DMDKhP7");
        sb.append("5/cizP3xUbGVoNRjZmaC8phJ71RfGzdo4V9Y0q9vXc2AhXz/mxAW0J6xJQTIrPzffHMiWp");
        sb.append("E1D+0Fr7Wufc2oqgYDphAvikjODjgljeP0sYQZEjzB+dQZjD+v6yhqysgPTNsRGunrsIQ4");
        sb.append("znxedpMZ7f6YTe6xMbqDMcA/wZ+Gv8++tZlwSn5JVxe9WsIQw1bGSuZJEwfG6LGq8/Hxvr");
        sb.append("JlYJPtJ4jyoA2cA9labp5wuFQtPDGrz3a4wxv2qlQuC9v8cY8x5CS2zJTGPMgd77K4dxPM");
        sb.append("eWZ/bKqnAbY74IvJ116UchDBXJCkB2iYVQyaJY2W/2+J+nesradrm5xeuywhhzDmEYVWl4");
        sb.append("Qy7+/+kNtmxUaqnJ2hgzEfh2xa8HgLd77+c1so2enp5lxpiTYyVl37KXvk3oIavlA/EhU+");
        sb.append("6vwJn1ginv/QBwSsyadQCjPz47y+o0TQ8rFAp1h6AVCoVrjTHHAz+oeOnMWgFI6d6LjRql");
        sb.append("JBETgQOMMT+s7G3JuPavZmjr6h8ZOlF2pFrnujvkenVX3D+5DrqfOuEZ/PtYEaxWCeoi9G");
        sb.append("A2Gvh+usbvnwGubcP6C8NirT2sIrCvZi0h8cOPgBXOuWo9ld5aewUhS+Q3GqjI00CZvjQ2");
        sb.append("FP21bFtdhAbGo0oV9DgBu17v88K4T4sqA4donrX22hiMXcTgIeSVXhUr5ZUNhNsydKhw+f");
        sb.append("f/MuDoGqmcH46pcC+Px3cRNVrzY0rnz1F/ztDnCD3zA865YpXt/BP4IWG0wVEZ25kCfNpa");
        sb.append("e5tzbqW19qIYBJTfu/+VEYC8GMv32+rdE865XmvtqzMCkKedc8daa2sFIP2MYwpAhi8Fri");
        sb.append("4UCte0uoFhtkYuAn4TW2JKDm2lQh/9w3t/XhPvf5qQ/euYst/tOHfu3Kk9PT21KkqVq+yu");
        sb.append("iK0+HWU418V7/4Ix5vcMzgaztTFmTqMV/zY5htB6Xu5UmhjXXRZsfiO27PzfdTbG/Hu1Xr");
        sb.append("+Ysvo/Kh4qq4ELmxxG9FngDtoz5KFdTkmSZEkT5+6HMQgpHy64gzFmP+/932r8zX3GmFsY");
        sb.append("nKVuD8IwyWsb/Oj3MXj+zhrCcLEXR+EcPUzIsDSdsU8gUKxoqLiR0Es71nKE+XW9Y7kTzr");
        sb.append("kbrLX3Z1SCjm0kALHWbge8tcbL91eUHWMRfHTHACkrUcrzwHHOuYsbOG998T4/wlr751i+");
        sb.append("tWoe8Joa6W/nERZ1LDmrzrZuB97jnHu4zv73Eyab7w/cRO2egUnAwdbaC5xz5Q1H0xicEb");
        sb.append("TSj7PWkYnBaB+hFf/VcZhVNXtRf67OYc6539U53oEYHHzMWruUkLK4lgNjYDQ/nqf+inup");
        sb.append("v06dsC/eH42WT1nbotX1eBSArP9eZGjWmtF7shaLa3K53I0VAci+w9jkyU1WTNcYY/5eEY");
        sb.append("Dkcrncy2PBWauFqdzmhInlz65n98Z8wpCXl5UFXjtnnJeRUDnB8WnCnI9WWk7uja06e5dV");
        sb.append("oN5BGKJQ6eUMHdZxD6Fru5n7685YEd+vQ67pc/H8NbsQ5jcIrXzlPgz8LeNvfg28h3Vjpb");
        sb.append("cE3pDP5/+SMX+kFADOiuVArqIiMyqTz6dd9sAzwCWd+KU89QAWxYYbWedc4N+o3mK7tbX2");
        sb.append("YOdcvZXRT6rx+z7gsiYqZCPlHYQMT7UMAF9pJPioqIwmzrlfDWO/ngAObGTtDWvtAQxeg6");
        sb.append("TSs8CX6gUfFRXzB6y1RxEyedZqsX8VYX7j1RX1x4l1AuxmAuFaZdqn6/zpafWCjypK65sd");
        sb.append("kPGejzN42LGMQAuMDM+zWePoR1pPT0+RkIGnXKvzIlbECmaznqqI4nMxoKjlIQa3+u0MHJ");
        sb.append("/P56etZ/fGA6xbUA5CT8i2o/XhMT105efdSmh1bVqapksZvOhRriwYqbRDxT2Q0vqq6ud3");
        sb.append("0DW9iYxsYxn+xtDVkN9QJ/i6kaFjfN+VJEkj3+/XMbj3ZAC40Xv/hIpsqeKijPu6G/hQnY");
        sb.append("rxJgxNvFH+XPnpWB5cTCe7H0N7g8v93jnXdGNiG4aVnUr1+aHVHE7ticcpcJVz7i8tHMPV");
        sb.append("MQCpZTOGNigVqZ2CHsKwvuFet82p3atGrPuc38Lx9hF6ktaO5P5LNvWADF+h3RucO3futF");
        sb.append("wu91rCRKftCF3jU+L1qlbYzayyjS17enqaXTBuAdndgbWsiQ+Z0jyQejne/0oYr7lZ2fuP");
        sb.append("SpJkD2PMb4E/eu8Xd+LFzufzeyZJMpeQLnYrQq9GaZ2QymszicGrAU+o8wBst1cweG4OhC");
        sb.append("xeLQ13KxQKq40xlS1rW1cJfCbE81OuNwY/rQQ+1yZJx0wBubvF87cK8MA+5Q91Y8wWdYKy");
        sb.append("8xg81vgVwKuNMY/VyngW1+I5gMFrODxP6FEZHb9cNjGWW910xhouS/jgpgMA6TVsTPvWEB");
        sb.append("iOhDBsZ2nylrE9RzEz1bmENTGq7WfeWruHc25BjU0czbr1RSpd6Zx7ZozP9WzCUJ4sJ47B");
        sb.append("ft0L3NhIEGOtnRSDgFxGAHL2MPbl64Reolp2t9ZOK0tR+yKhx6VWhqnDrbUPOedOHsY+va");
        sb.append("XO65cTGjRbsRD4O7WHd21urX2Jc+5BVXMVgHSqtnXlG2NeQ1gp+t9i5WEi1RfBa+TBNpXG");
        sb.append("sjCUe7rFAKSyJSTJure897cZY64H3ltxL76G0C36FWPME4TUtzcSsvYsG6sLHFfx/iIhM9");
        sb.append("gW8bpMpPoCivVsNIq7PrtKpeAtxphSxbD8elX+f1LltZTBreoA3XPnzp3e09NTPjZ4IkPH");
        sb.append("BvfRYgacQqGw1BjTS2ekQ1zS4t/1EtY7KQ9AcoTJn89kfFcuMsacXVFhPoqQ/a7WkJZtGL");
        sb.append("q2w3zv/fxRPE+7Elq9X9pimdJO/YTJsk/Ffx9MSD891lFtFyHDzodiI85Y+06NAARChsS3");
        sb.append("xUaqaj5Yp2I71rYiNOjVcoVz7vEx2C9PSLvd6HcqqwHraefccBpE58eGihk1Xt+JkBWrFI");
        sb.append("AsjfWfrTLu7y9Yaw8F/ts599sW9ilr7sdKwLfaA+Wce9Jau7DOZ/xbE9dHFICMumFPno6L");
        sb.append("xJ1BWPegLXK5XCvXtp0TnTIf7n19fR/o7u7etUqFdlL82Zww7vSEeI4eImRcuqhYLN6Zy+");
        sb.append("WKGWtetDP4eCfwc9rXczGaebs3r1Jp35PaKTdbvNVyk6qUK5WLVBbTNH16GJ+zhMG9SWNh");
        sb.append("LWHtlFYMVGkQyNFAeuE0Tb+bJEl5Je7taZrOospQunw+nwPeVOX8/2AMni0z6Iyehr6KRp");
        sb.append("yNYkNCJ3SrTeuQ/cA5t8JaewHw0Rrl+f7W2p855wZ9j621hzB4PapyNzvn7u+Aw9uyThl+");
        sb.append("+Rjt1+IaWapqBYGbZLz+/4a5LwMxINo/o2Fjctn9ssRa+/9Yl9K4mu4Y+F1irV0MfIuQtK");
        sb.append("a3WqaqKvJZAVds1BmO+2P5UCtj3ytUxR05mgMyfMOqtBtjtoxfyGrBRzFWeJ4ipM58OEbj");
        sb.append("5T8PECY6jysLFiwotUr+iNACXC+Y2BGwwLxcLvdP4J3GmOkjGHgkxphjCCkqqz24St3Pj8");
        sb.append("eK4ENVrsvDjG2Gmymj8BldVYKqhKE9PWmSJKuG8TkrO+C27R/G9SzGe6ZSIz1i36v8fiRJ");
        sb.append("YjMe+EdX/O5J7/0fVFRLA76f8drrCCt3VzqE2q3m3xrrA7LWlnoasxTGYNfWECagN2pmeQ");
        sb.append("BQxV3D3J9inQr9pgxt0PoNja9F8TLCfI2ngC9ba7eL1yZL1oT75W2o+zxFGB5byywVCQpA");
        sb.append("OtlwW6/OIHRtl3s2VnxPJeSbPowwFnK/+BB4bdnPvxNSlY473vvUe//JeGxnATfQ2BoFex");
        sb.append("FarM43xrx8hHbvP4BvMrSX8DZCLnFLGHbwTkKL0eurXJcDYwtLp9ybxXh+FxLS8Lbj515q");
        sb.append("DwWSNigUCssZOtHyqHw+Xy14yTO0V/EsnUVp0P2Eoa/VTAbeUr4mgbV2DwYPKyz3UMa2Rl");
        sb.append("MX9XsaxyIDY1+TDSsbk722TjuGkGUNdZ5MRWOTc+5O4GusG5bViGmEYXl/B46LK9PXMr1O");
        sb.append("ALdimMe7ss7za5qKhJGjIVhjyBizL2FeQblbCPMNbo0rUTeynRfG83nw3heAQuwNehmhO/");
        sb.append("+VhJVeDbVzjR8GbGOMOdR7/1Qbr8tk4FMMbdX7AnCx935Jg9uplyVkpK2tEoD8DLiQ9q2r");
        sb.append("0cvQxSCrtfYn1J6k2ogZHXCrdtP6HJ4cQxMzpGS3vpU7j8FrV0xNkuSjVKQAT5KkMhVqX5");
        sb.append("qmP0WkMasIGbFqDcM5krDIWqnCvjdDFxst+X6HNE7k6lRk+xmbRAkpzc2P2qhOnW1VG/Yp");
        sb.append("Kz17QpUGV+fcr621KwmLADYzTHY7wqT5N1lrP+Kce7aFfe1rw/EW6wSvogBkvVQ5jGIpcE");
        sb.append("QzGaCMMQlDx3uP10BkaTwH/8zn8xOSJNmYMIxoNmH12o8yNOvSawgZtdrZC7QFQyfxnu29");
        sb.append("P6PJ7cxgbBfQey4WsBPKvu/93vsHRvhz+xnaothF6M5udSL0dh1wi04cRiA0ocq9W6TxRB");
        sb.append("GLCQu5lWeFOaE8ADHGbAe8fVANJ00vLBQKz6uolUbEbFi3AndTfdL2VoT00Zdaa6cDb66x");
        sb.append("qWXA9Q2O8x8NXXW+m8k4uDylynKtY2nHkNus59UaajSoOeeusNbOi3WaE5quyca9AAAT9U");
        sb.append("lEQVT4vIQwiuB6a61p8n7pakMddmKde2OFSoWRbRmQsVOZfeGnwKNNbqOb2i1Q41ahUOj3");
        sb.append("3j/vvX88rrNyEqF35LSKFosE2NcY0871NV5b5XdfbWE7s8meNDjSljC0VeylxpiRDop6Cf");
        sb.append("NfKh/yu7SysXw+v10H3ZqzjTGtlJuTqnxPB4rFYkONDd77FxiaRndbY0xlQDL46Z4k56qY");
        sb.append("lSaDkHsJqdJr+UT875aVAW+ZP9E52YNS6g91mjIOLs1qsnsothnm9us1Zj5Pxhw459yjzr");
        sb.append("kTYzl/Gc31/s9h6EKt1NnGZIY/RGoa2dkVn1SJoABkvTN37tyZVW782733zU5yncjgdQLW");
        sb.append("S3G+yIve+69Uq4iRPVmtWZWZLx703rfSErIboTdlrNzD0DG9e1B7SFu7rtUAQ+e+TCSkNG");
        sb.append("z+qZgkb+2gW/FVtNbjuAlDM7o8W5G+uJ5bGTzRdAphNXXmzJnTxdBUqDfQeo582bD9mdrp");
        sb.append("od8cez9eT/VhlX3A1c65Tmk9HqD+4qF7jINr8hzZC+e9qg0BSFZj5tM0kC7aOXefc+6QuK");
        sb.append("1z49/1N/D574tziso9Vid42GqYx7w12WuW3a+iQAHI+qhay3jTYzjTNN2OOisqr4euZugK");
        sb.append("49NH8Nosb3YD+Xx+CrAv2ZMGm9F0r4X3fkGVAnxu/BlpjzA4TWwC7G6MaaWV7sgOuvf+nd");
        sb.append("ZWs68WRF3V5PW8l7Cievk53csYM7urq+uDDB0e9tve3l4Nv5JWXEf2Glcfovbq6Hcy/JSw");
        sb.append("beOcGyBkJWz2+9lpHie7J+d1w9z+BLLT3i6miSFJzrkHnXPHALsDpzdwDRKGpoC+M+P9Wz");
        sb.append("F0wdtmvYzsIVi3qihQALI+Wl3ld9s2O7wjSZJvboDnbhlDW1TSEbw2Ta8/kSTJPtQenlDP");
        sb.append("QI3CthW/q7K9z+Xz+akjfI0WMXTRsp0J430bZox5HS32nIyQ6WmavreZP9hjjz0mAF+q8t");
        sb.append("IFLXz+lQyeX/MS4FDCnKXyIPUB4O8LFy5MEWm+0t4Xy45aLdfHUX2oahG4yTn3aIcd0pNk");
        sb.append("NyS9z1o7tcMvy6I6x7C1tXY4ZeXeZA9Fu5MW1kFyzj3tnDsJOCg2oKQZ9dHK1epvytj0VG");
        sb.append("Bva21LE8WttdvE4KiWNc65u8b4mq/XdXQFIGOkp6fn8SoVw0NpIuuCMeYEhk6W7mj5fL4d");
        sb.append("WSVeWVFQriA7fWCz7qv49zRjzH80cV2mEVYVbjXrUz9Dey5e3VIkMzDwA4aOY907SZIRXZ");
        sb.append("guDlm7jsFd9hOBY+Nk6UbO4yTg23RYJpIkST5vjJlT73177bVXAtDd3X0WYRGxilPke1o4");
        sb.append("r1czeFjABEIShv0q3npzf3//QpW0MgznUrtXfmeqTwBeTvWx/GNtCZD1fZtCXLHdWtuRE9");
        sb.append("Kdc0+RPa8miWVBq07JeG0lMK/VVcfj/t9FGCaaNcx844rz//s6m303rS9Qu1uNILrksg64");
        sb.append("7Ot1GmAFIGPr7xX/fgfQUOuqMeaThG7NcSVJkmnGmEuMMd/M5/NNT9A2xrwUOJzB82cerR");
        sb.append("I0DEe1VpcLjDF1U7DOnTt3MnA9w1ttfC1Du56PMMY0XRGfN29eP9UXuTzSGHPpNtts0/TD");
        sb.append("ds899+w2xhxvjMls5SwWixcytNt9d+B/6k2Ej8d6Np05v6kLuG7u3LmZw8nuuOOO1BhzPH");
        sb.append("Bs5Wtpmp44jM+/iMEt07MZPATxBeCq+fPnq/dDhlPhXdNCMHG3c+6ODjyWx4F/1Hnbp6y1");
        sb.append("bxxOJXsUXE7t1LMJ8EZr7UHNbtRa+2ayh3LfA9zehuuwhPrzKsrrpYvjZ9eyA3CMtXZCk8");
        sb.append("e7MSFjV9Yz9bxRuqZZ99v09bmMURresXUOQ9MY/jJWss+JBU0pR3kO6E7TdMskSU5m3VjJ");
        sb.append("AUIL97bj5JhTYDPgy0mSfNkY8wfgF8A/CWtH9MVjSsuOu4swvMQAPwB2LdteP3CN9/6ZNu");
        sb.append("7j04TW+/Jr81LgDmPMh2OBOBB/kvg96gIOAH4Sjw/CJM6pZK9eW81qwgJe5eOStwHuNsZ8");
        sb.append("K03T24HnkiQZqHj4LPfeDxna572/2hhzBvD5ipfePWvWrEdmzZr1mTRN/5okyRrWpXosnf");
        sb.append("scYR7LxDRNd06S5MPAEYQWw8wJiT09PcuNMV8nrD1SHrwdCNwSz+UD8ZoXyz5rK8JaA4fG");
        sb.append("968htKzOHON7d028NjOALXK53H3GmA8R5iQNsC472wTCPKLPAZ+p0tDz81wu989Wd6Kvr+");
        sb.append("8n3d3dXyesTFzNowMDA1eqeJU2OJ0w3KpR3+3gY/kzYd7KthkNC7+x1h7knLulwYpsEsvC");
        sb.append("bwIXOOcWjPAxXEDoqdmyxuubAqdZax92zs1v8BheUifQHACui8FD+d/tRRiBcTbwTBOBW9");
        sb.append("bzsDfO2SmvL3yP0BtXy6eBu6y1P3fO9TdwvBMJGQPfkvG224F5o3Rfrs54rctau79z7i/r");
        sb.append("Y+GiAGQMee//ZIy5mqET4L4GnBgrNotjBW1TYPckSfaruG4+Vsp/MY4OvbygOoR1w8jmEV");
        sb.append("bWXhIrnEXCMKYdCONTq2W6upE2r/TsvV9pjDmLkOWlvKV+N+AO4OZYQD0XX98+th6VDy1a");
        sb.append("FR/eHyCkGGzm8/uNMdfEe6D8Ybkz8LMkqdlpcZwx5n+892mVbX7BGDOD0BtSXiGeDVySJM");
        sb.append("lzhBbCxfG4Sud+K8JEvTlJkmxSpcWt3rH8rzFmH4amiJ1LmCNyUzyXy+Ln7Q68rWLbnwf+");
        sb.append("swMCkNWEdTfeRcg4M5UwTn4RIePUE/G7+fIYjG5WZRvzga/ccccd/a3uxIIFC3qNMT+LwU");
        sb.append("2lInDlvHnzVqmEleFyzj1mrf0ToXe+nqecc5d28LHcZK39WyyTa9kCuMpaewphvtWD1SrW");
        sb.append("1tru+Fx6DWHh4FfS2pyuZo+hz1r7LcIQ31p2I6zT8gXgZufc0zUq4psShiB9l+w5hg/FOk");
        sb.append("aljYAvx/LwdGvtP4GH4vyhWpX/N8TzVqtesKjieFNr7e9ikLFzxj6eD+xirf1JvGb9VT57");
        sb.append("QnyWHRd/0oxn2HdoIflMix7L2JcJwOestbdVZpWz1m4F9LewgKMCEInfuDT9WJIklwL7VL");
        sb.append("y0Wax0ZbmJ0BOy3Tg65ITaGZ3mNFlZ/wPwKe/9iyOwnzcQxsSextAW7NeRnXGklzDp+DcM");
        sb.append("TY3aqDvj53+fxntQ1tZ5/XhCj8MXGNp6vhmhZ6IZDQ0J896faIzpo/pikfvGn1rO9N5/3x");
        sb.append("hzVAfcu93ALYTeqYsJCytCyHvfyBon84GPeO+XtGFfzqoRgPTVqCyItOq0BgOQs8bBsZxM");
        sb.append("WH9rVsZ7ZhBGIHwUuMlau4gwymB1LItnx0r+a+J/yyvQo+FsQqNdVrm5U2wcuTYGBosJ63");
        sb.append("gU4/HtEOscb6Z+hsWvOueeqBEwFOM5+AUhRfjNcUHC0jlbFRtqZhHmMR6X8dxIgUuq/P4Z");
        sb.append("4FuE0QVZddbPEeaEXGOtvSs2CK2NgdLWcT/fXhYA1Qo+LgWuGsWheAXgPTWOLYnX6FJr7e");
        sb.append("UxWOmOz5s3xnPym/FasGyIc0ASBg8HKUXyuRb/dlhBXKFQWEKY03Bxk396JnC49/5+huax");
        sb.append("ntzg8Uyq8+9m7qPJFdupVcisioHDomGctkeB/wKO8t4/1sD7J1epSNarNPfFlqGPxIK7Uf");
        sb.append("OAA73334+Fc9OfHT8/BX5O6A24tMGH24Q62+yN982bgQuHcf4fj4HRm5r4m68Q1qx4osH3");
        sb.append("Pw8cHSsM1e7N7lEoK3IV3/fJwMbe+xvig+y6Jhoafg4c3MrE8xrX8gngl1Ve+nObAhyRkh");
        sb.append("5Cz2+WfsJCuh3NOfcgQ1O91rJnrDCfQ2hhvzD+97uEnuTdxugYUuCoWA7XcwBhEd1zCT00");
        sb.append("FxLmNnwjNjhNyggGAE51zv2qwV3bLT6XvxfrM5cTUo1fHv/9LbLXoFrgnLuixvH+ukZ5V+");
        sb.append("ml8blxTrwfS9fsnPj7Her8/WLgy8655aN4Sa9o4Dl0QDyGn8Xj+Saw/zDqbB1hQ+wBWRa/");
        sb.append("iNvHCmISf9dIRXYloXVxM9aNWb+tDZWJh+bMmXNkV1fXj2KB964aN9aDwG+BC9I0XVwoFP");
        sb.append("rLvjQ/Yt1Ywj7CMJp6fh0rzMVYobuNxhYMqrQkFspTY8HVD9xdqxJsjPlBjNy3SNP0jXFY");
        sb.append("2d5ktyI/S+iV+BXwtzRNXygUCsUG9+8c1mWkmkhIBTjQwHXpNcb8krCq71HxwVVtH/tiZf");
        sb.append("Q84G9lixauiYXFtvG8TCC0njd6X/QDNxpjbgM+Sejm3511iyflKoLoedWGX1Vsswj4mMTg");
        sb.append("K8D7gYNja96EjKDxDsJwt6sIw+RWN7Nopve+N5/PX5QkyTWEXqFPUL1L/V+xNe2nwFNxf4");
        sb.append("n/3j5et27CRP+R9iChtXFqLCfWxGPHez/PGHNoPG+fjg+IyqD7ReD3aZo64O4WFhltZP8q");
        sb.append("A53TVF+WNltLGHqYNcTogiYbasYyCLnaWns4Qxe0zaoATu+wY/hXPIY/NrhvU2hutfckPt");
        sb.append("NbSfPfTXYPUy3vzzjetdbaYwlDqPZtYFtdDF0XqZFGr4Occ/8a5Wu5yFp7SwPHNYHBw3pT");
        sb.append("Rq/XTQFIO8TJyq7Fv10BnDoS+xWzFd0Uf8jn8zNjxbULWN7X1/fAwoUL+2vs1z2xgtrs8Z");
        sb.append("zXpnP6UKzMNvr+vlhpXxkfXP/3YMvn85sDWyVJMiVN0zRJkheAR4ZTefPenzyMvy3G4OcM");
        sb.append("4Iw5c+ZMzOVyLyGkx+tNkuRJ7/1TNf52JaHHYbjnd3UMLv/v/mjDNtfGFrTvxB+MMZunaT");
        sb.append("orSZLJaZoOAM8PDAw8Nn/+/LZUnAuFQgo8Fb9/bu7cuZOSJNkpSZKpaZou7+vrW7xw4cK+");
        sb.append("Gvt71hiUFYuAkzJeXxkDz+vivbtdkiQzAYrF4uM9PT1PjPAuHl3x77sLhcLtiLS3glS01v");
        sb.append("4DeJjaLcg/6fDsUZXH9L/W2iKhhXxymzabjPIx/D9r7WHx+dnOYdhpfN6dnDWfo43HnAJv");
        sb.append("d84tqnO8LwKvt9ZeHRt82nm+Hwb2c849NEa35CfIzva1XtIckA5VKBSeJmRj2tCO+1kGL7");
        sb.append("TWUebNm9fL8IaPdXJwPqrnvqenZy1h3PD6cu8+ShgeOOKMMQcTJsyWOx2RkakQP0ToCT6m");
        sb.append("ymt/pf4q183u30i8VlmhvcRa+zhhraFXMzi1e7N6qZMVcCSuj3Pu+phC92zCnI4Zw9hckT");
        sb.append("By4Szn3M9H6RgWAZ9yzl3bxDG/1Vr7RUJ689nD/PznCEOgjnbOrR6Fe7fWMd1rrf04Yfja");
        sb.append("5Cb2IWEc0zogIiLjzxcq/v0MIWuPSHmFMq34gRaGbTjn1hKyMi4r20ZpO5c6555rcf9oYR");
        sb.append("/TGn+Xlh1zo8d1M2Ge3YmENL3NHscjwP8ShpUurrPP1fZ12Jxzi5xzbyPMv7iExoaTl1tF");
        sb.append("GJZ8MqEnotHg40FC7/ntZKeSraaHkNzgrc0EH2XH/G3CHMTTCcODmz2X9xKGgb/XOXdkk8");
        sb.append("FH1j1Iq9fVOXc+cCSNr7eymnEy7LEW9YCIiIwj+Xz+jYQ1ccr9OE3TFTo7Et1CWNR2Ykbl");
        sb.append("qVk3AAcxOOlJkZDFp1n9hLln1ZKV5AhDYmpl9VsQg4ZqxzFAk0NZYnrTc621lxDm+O0Wv1");
        sb.append("+7ENZfKs2d6CP0ED8WK7AFwqJ698RFG6u5i5CwoljjHNzdrgsee3T+SJgruCsh1fmuhOFZ");
        sb.append("02N9LwVWEDJU3U+YA3oPsKhGpqusz3scsNbaHYEdCfMz9iCkIS99Zvl5ezheu0L8vPuHeb");
        sb.append("yLgC/GdUxeTkgasCdhZfSZrJtH2xs//xFgYTzm+4H7Glk3pIYr4/FUm0va8iiJeA1vIcwt");
        sb.append("3I+Q7n2beCwDwNK474UYeBXGcyE1rrtvREQ2JHvttVcuTdMbCGvUlCwD3u69/2fH7Ogvl+");
        sb.append("UJ6SF36oC96QN24IObPgGQXsPHgB93yPPvGuCQ5C1NtyDLCLLWTo6VvgmsGymSxkpgH7DW");
        sb.append("Odfb4ccwKR5DdzyGpCxo7I8B3pp2zt2x1m4Ug97usuCydN564+f1j9DxdhGSsUyM1y2p8v");
        sb.append("lrG5jX0mn3YHfZsfTH41hdsWCjAhAREWkPY0z5GN+0WCxOyeVyZxKGWpQPn72gWCwe29PT");
        sb.append("s6Zjdl4BiAIQEZEMGoIlItJ5wcdsQparbQgteJvmcrnXMXTi+ePAhR0VfIiIiCgAEREZdz");
        sb.append("YH/pMwtjrL/3jvb9LpEhGR8URZsEREOk+R+mk9T0Kpd0VEZBxSD4iISGeWzdOq/P4hQrrQ");
        sb.append("nwB3eu/7dapEREQBiIiIDNc8YHvCROnKLDWp976oUyQiIgpARESkLbz3pdSRIiIi6x3NAR");
        sb.append("EREREREQUgIiIiIiKiAEREREREREQBiIiIiIiIKAAREZENU9LB+5HoHImIjB1lwRIRkXZb");
        sb.append("CzxCaORKx3hf+hmcUWw5sLgDKv9dwBMdcH5ERBSAiIjIuPcAcCwwqQP2JQWWlf37L8BhHX");
        sb.append("KeVgC9ul1ERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERE");
        sb.append("RERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERE");
        sb.append("RERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERE");
        sb.append("RERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERE");
        sb.append("RERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERERE");
        sb.append("REREREREREREREREZJT9fyDKcSpxaoHNAAAAAElFTkSuQmCC");
        return generateAttribution(sb.toString());
    }

    private String generateAttribution(String base64Image) {
        StringBuilder sb = new StringBuilder();

        sb.append("<a href='").append(ATTRIBUTION_REF).append("'>")
            .append("<img src='").append(base64Image).append("'/>")
            .append("</a>");

        return sb.toString();
    }
}
