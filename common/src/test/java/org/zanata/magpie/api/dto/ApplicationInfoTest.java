package org.zanata.magpie.api.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ApplicationInfoTest {

    @Test
    public void testEmptyConstructor() {
        ApplicationInfo info = new ApplicationInfo();
    }

    @Test
    public void testConstructor() {
        String name = "name";
        String version = "version1";
        String date = "10/10/2010";
        Boolean dev = true;
        ApplicationInfo info = new ApplicationInfo(name, version, date, dev);
        assertThat(info.getName()).isEqualTo(name);
        assertThat(info.getVersion()).isEqualTo(version);
        assertThat(info.getBuildDate()).isEqualTo(date);
        assertThat(info.isDevMode()).isEqualTo(dev);
    }

    @Test
    public void testName() {
        String name = "name";
        ApplicationInfo info = new ApplicationInfo();
        info.setName(name);
        assertThat(info.getName()).isEqualTo(name);
    }

    @Test
    public void testVersion() {
        String version = "version";
        ApplicationInfo info = new ApplicationInfo();
        info.setVersion(version);
        assertThat(info.getVersion()).isEqualTo(version);
    }

    @Test
    public void testBuildDate() {
        String date = "10/10/2010";
        ApplicationInfo info = new ApplicationInfo();
        info.setBuildDate(date);
        assertThat(info.getBuildDate()).isEqualTo(date);
    }

    @Test
    public void testDev() {
        Boolean dev = true;
        ApplicationInfo info = new ApplicationInfo();
        info.setDevMode(dev);
        assertThat(info.isDevMode()).isEqualTo(dev);
    }

    @Test
    public void testEqualsAndHashCode() {
        String name = "name";
        String version = "version1";
        String date = "10/10/2010";
        Boolean dev = true;
        ApplicationInfo info1 = new ApplicationInfo(name, version, date, dev);
        ApplicationInfo info2 = new ApplicationInfo(name, version, date, dev);

        assertThat(info1).isEqualTo(info2);
        assertThat(info1.hashCode()).isEqualTo(info2.hashCode());

        info2.setName("name1");
        assertThat(info1).isNotEqualTo(info2);
        assertThat(info1.hashCode()).isNotEqualTo(info2.hashCode());

        info2 = new ApplicationInfo(name, "version2", date, dev);
        assertThat(info1).isNotEqualTo(info2);
        assertThat(info1.hashCode()).isNotEqualTo(info2.hashCode());

        info2 = new ApplicationInfo(name, version, "11/11/2011", dev);
        assertThat(info1).isNotEqualTo(info2);
        assertThat(info1.hashCode()).isNotEqualTo(info2.hashCode());

        info2 = new ApplicationInfo(name, version, date, false);
        assertThat(info1).isNotEqualTo(info2);
        assertThat(info1.hashCode()).isNotEqualTo(info2.hashCode());
    }

}
