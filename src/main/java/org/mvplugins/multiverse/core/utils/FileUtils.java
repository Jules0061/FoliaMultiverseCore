package org.mvplugins.multiverse.core.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dumptruckman.minecraft.util.Logging;
import io.vavr.control.Try;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.config.CoreConfig;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;

@Service
public final class FileUtils {

    private final CoreConfig config;
    private final File serverFolder;
    private File bukkitYml;
    private File serverProperties;

    @Inject
    FileUtils(CoreConfig config) {
        this.config = config;
        this.serverFolder = new File(System.getProperty("user.dir"));
        Logging.finer("Server folder: " + this.serverFolder);
    }

    public File getServerFolder() {
        return this.serverFolder;
    }

    public @Nullable File getBukkitConfig() {
        if (this.bukkitYml == null) {
            this.bukkitYml = findFileFromServerDirectory(config.getBukkitYmlPath());
            Logging.finer("Bukkit.yml: " + this.bukkitYml);
        }
        return this.bukkitYml;
    }

    public @Nullable File getServerProperties() {
        if (this.serverProperties == null) {
            this.serverProperties = findFileFromServerDirectory(config.getServerPropertiesPath());
            Logging.finer("server.properties: %s", this.serverProperties);
        }
        return this.serverProperties;
    }

    private @Nullable File findFileFromServerDirectory(String fileName) {
        if (this.serverFolder == null) {
            Logging.warning("Unable to locate server directory.");
            return null;
        }
        File file = new File(this.serverFolder, fileName);
        if (!file.exists()) {
            Logging.warning("Unable to locate file from server directory: %s", fileName);
            return null;
        }
        return file;
    }

    public Try<Void> deleteFolder(File file) {
        return deleteFolder(file, Collections.emptyList());
    }

    public Try<Void> deleteFolder(File file, Collection<String> keepFiles) {
        return deleteFolder(file.toPath(), keepFiles);
    }

    public Try<Void> deleteFolder(Path path, Collection<String> keepFiles) {
        try (Stream<Path> files = Files.walk(path)) {
            files.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
                if (!isDirectoryEmpty(file)) {
                    Logging.finest("Cannot delete folder as it is not empty: " + file);
                    return;
                }
                if (file.isFile() && keepFiles.contains(file.getName())) {
                    Logging.finest("Keeping file: " + file);
                    return;
                }
                if (!file.delete()) {
                    throw new IllegalStateException("Failed to delete file: " + file);
                }
            });
            return Try.success(null);
        } catch (IOException e) {
            Logging.severe("Failed to delete folder: " + path.toAbsolutePath());
            e.printStackTrace();
            return Try.failure(e);
        }
    }

    private boolean isDirectoryEmpty(File file) {
        if (!file.isDirectory()) {
            return true;
        }
        try (Stream<Path> entries = Files.list(file.toPath())) {
            return entries.findFirst().isEmpty();
        } catch (IOException e) {
            return true;
        }
    }

    public Try<Void> copyFolder(File sourceDir, File targetDir) {
        return copyFolder(sourceDir.toPath(), targetDir.toPath(), Collections.emptyList());
    }

    public Try<Void> copyFolder(File sourceDir, File targetDir, List<String> excludeFiles) {
        return copyFolder(sourceDir.toPath(), targetDir.toPath(), excludeFiles);
    }

    public Try<Void> copyFolder(Path sourceDir, Path targetDir) {
        return copyFolder(sourceDir, targetDir, Collections.emptyList());
    }

    public Try<Void> copyFolder(Path sourceDir, Path targetDir, List<String> excludeFiles) {
        return Try.run(() -> Files.walkFileTree(sourceDir, new CopyDirFileVisitor(sourceDir, targetDir, excludeFiles)))
                .onFailure(e -> {
                    Logging.severe("Failed to copy folder: " + sourceDir.toAbsolutePath());
                    e.printStackTrace();
                });
    }

    private static final class CopyDirFileVisitor extends SimpleFileVisitor<Path> {

        private final Path sourceDir;
        private final Path targetDir;
        private final List<Path> excludeFiles;

        private CopyDirFileVisitor(@NotNull Path sourceDir, @NotNull Path targetDir, @NotNull List<String> excludeFiles) {
            this.sourceDir = sourceDir;
            this.targetDir = targetDir;
            this.excludeFiles = excludeFiles.stream()
                    .map(sourceDir::resolve)
                    .toList();
            Logging.finest(this.excludeFiles.stream().map(Path::toString).collect(Collectors.joining(", ", "Exclude files: [", "]")));
        }

        @Override
        public @NotNull FileVisitResult preVisitDirectory(@NotNull Path dir, @NotNull BasicFileAttributes attrs) throws IOException {
            Path newDir = targetDir.resolve(sourceDir.relativize(dir));
            if (!Files.isDirectory(newDir)) {
                Files.createDirectories(newDir);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
            if (excludeFiles.contains(file)) {
                Logging.finest("Ignoring file: " + file);
                return FileVisitResult.CONTINUE;
            }
            Path targetFile = targetDir.resolve(sourceDir.relativize(file));
            Files.copy(file, targetFile, COPY_ATTRIBUTES);
            return FileVisitResult.CONTINUE;
        }
    }
}
