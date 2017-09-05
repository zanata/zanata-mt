package org.zanata.mt.backend.google;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GoogleCredentialTest {
    private static final String CREDENTIAL_FILE_CONTENT = "{\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"type\":\"service_account\"}";
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void isAbsentIfCredentialFileIsBlank() {
        GoogleCredential credential =
                GoogleCredential.from(
                        "", CREDENTIAL_FILE_CONTENT);

        assertThat(credential.exists()).isFalse();
    }

    @Test
    public void canWriteGoogleCredentialFileIfGivingCredentialContent()
            throws IOException {
        File folder = temporaryFolder.newFolder();
        File destFile = new File(folder, "some/new/file.json");
        GoogleCredential credential =
                GoogleCredential.from(destFile.getAbsolutePath(),
                        CREDENTIAL_FILE_CONTENT);
        assertThat(credential.exists()).isTrue();
        assertThat(credential.getCredentialsFile()).hasContent(CREDENTIAL_FILE_CONTENT);
    }

    @Test
    public void googleCredentialFileMayNotExistIfNoCredentialContent() {
        GoogleCredential credential =
                GoogleCredential.from(
                        "/Non/exist/file/path", "");

        assertThat(credential.exists()).isFalse();
    }

    @Test
    public void isAbsentIfCredentialFileIsEmpty() throws IOException {
        File file = temporaryFolder.newFile();
        GoogleCredential credential =
                GoogleCredential.from(file.getAbsolutePath(), "");
        assertThat(credential.exists()).isFalse();
    }

    @Test
    public void canWriteContentToCredentialFile() throws IOException {
        File file = temporaryFolder.newFile();
        GoogleCredential credential =
                GoogleCredential.from(file.getAbsolutePath(),
                        CREDENTIAL_FILE_CONTENT);
        assertThat(credential.exists()).isTrue();
        assertThat(credential.getCredentialsFile()).isEqualTo(file);
        assertThat(credential.getCredentialsFile()).hasContent(
                CREDENTIAL_FILE_CONTENT);
    }

    @Test
    public void canHandleInvalidCredentialFileContent() throws IOException {
        File file = temporaryFolder.newFile();
        GoogleCredential credential =
                GoogleCredential.from(file.getAbsolutePath(),
                        "{}");
        assertThat(credential.exists()).isFalse();
    }

}
