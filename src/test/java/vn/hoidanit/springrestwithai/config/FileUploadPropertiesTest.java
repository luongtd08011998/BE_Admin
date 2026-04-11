package vn.hoidanit.springrestwithai.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FileUploadPropertiesTest {

    private String originalUserDir;

    @AfterEach
    void restoreUserDir() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    @DisplayName("relative base-dir + user.dir ending with /target → uploads next to project root")
    void getUploadRoot_userDirInTarget() {
        originalUserDir = System.getProperty("user.dir");
        Path fakeProject = Path.of(originalUserDir, "fake-maven", "BE_Admin");
        Path targetDir = fakeProject.resolve("target");
        System.setProperty("user.dir", targetDir.toString());

        FileUploadProperties p = new FileUploadProperties();
        p.setBaseDir("./uploads");

        Path root = p.getUploadRoot();
        assertThat(root).isEqualTo(fakeProject.resolve("uploads").normalize());
    }

    @Test
    @DisplayName("relative base-dir + user.dir ending with /target/classes → uploads next to project root")
    void getUploadRoot_userDirInTargetClasses() {
        originalUserDir = System.getProperty("user.dir");
        Path fakeProject = Path.of(originalUserDir, "fake-maven2", "BE_Admin");
        Path classesDir = fakeProject.resolve("target").resolve("classes");
        System.setProperty("user.dir", classesDir.toString());

        FileUploadProperties p = new FileUploadProperties();
        p.setBaseDir("./uploads");

        Path root = p.getUploadRoot();
        assertThat(root).isEqualTo(fakeProject.resolve("uploads").normalize());
    }

    @Test
    @DisplayName("absolute base-dir is unchanged")
    void getUploadRoot_absoluteBaseDir() {
        originalUserDir = System.getProperty("user.dir");
        Path abs = Path.of(originalUserDir).resolve("abs-upload-root");
        FileUploadProperties p = new FileUploadProperties();
        p.setBaseDir(abs.toString());

        assertThat(p.getUploadRoot()).isEqualTo(abs.normalize());
    }
}
