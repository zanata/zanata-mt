package org.zanata.magpie.api.dto;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * JSON entity for information of the application
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ApplicationInfo {

    private String name;

    private String version;

    private String buildDate;

    private @Nullable Boolean devMode;

    @SuppressWarnings("unused")
    protected ApplicationInfo() {
    }

    public ApplicationInfo(@NotNull String name, @NotNull String version,
            @NotNull String buildDate, @Nullable Boolean devMode) {
        this.name = name;
        this.version = version;
        this.buildDate = buildDate;
        this.devMode = devMode;
    }

    /**
     * Name of the application
     */
    @NotNull
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Build version
     */
    @NotNull
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Build date
     */
    @NotNull
    @JsonProperty("buildDate")
    public String getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

    /**
     * Is in Development mode
     */
    @Nullable
    @JsonProperty("devMode")
    public Boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(Boolean devMode) {
        this.devMode = devMode;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationInfo that = (ApplicationInfo) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(buildDate, that.buildDate) &&
                Objects.equals(devMode, that.devMode);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, version, buildDate, devMode);
    }
}
