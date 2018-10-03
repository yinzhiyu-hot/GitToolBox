package zielu.gittoolbox.lens;

import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.LocalFilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.impl.UpToDateLineNumberProviderImpl;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitVcs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.MetricsHost;

class LensBlameServiceImpl implements LensBlameService {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private final Cache<Document, CachedAnnotation> annotationCache = CacheBuilder.newBuilder()
      .weakKeys()
      .build();
  private final Timer fileBlameTimer;
  private final Timer lineBlameTimer;
  private final Timer annotationTimer;

  LensBlameServiceImpl(@NotNull Project project) {
    this.project = project;
    Metrics metrics = MetricsHost.project(project);
    fileBlameTimer = metrics.timer("lens-file-blame");
    lineBlameTimer = metrics.timer("lens-line-blame");
    annotationTimer = metrics.timer("lens-annotation");
    metrics.gauge("lens-annotation-cache-size", annotationCache::size);
  }

  @Nullable
  @Override
  public LensBlame getFileBlame(@NotNull VirtualFile file) {
    return fileBlameTimer.timeSupplier(() -> getFileBlameInternal(file));
  }

  @Nullable
  private LensBlame getFileBlameInternal(@NotNull VirtualFile file) {
    GitVcs git = getGit();
    LensBlame blame = null;
    try {
      VcsFileRevision revision = git.getVcsHistoryProvider().getLastRevision(createFilePath(file));
      blame = blameForRevision(revision);
    } catch (VcsException e) {
      log.warn("Failed to blame " + file, e);
    }
    return blame;
  }

  private GitVcs getGit() {
    return GitVcs.getInstance(project);
  }

  private FilePath createFilePath(@NotNull VirtualFile file) {
    return new LocalFilePath(file.getPath(), file.isDirectory());
  }

  @Nullable
  private LensBlame blameForRevision(@Nullable VcsFileRevision revision) {
    if (revision != null && revision != VcsFileRevision.NULL) {
      return LensFileBlame.create(revision);
    }
    return null;
  }

  @Nullable
  @Override
  public LensBlame getCurrentLineBlame(@NotNull Editor editor, @NotNull VirtualFile file) {
    return lineBlameTimer.timeSupplier(() -> getCurrentLineBlameInternal(editor, file));
  }

  @Nullable
  private LensBlame getCurrentLineBlameInternal(@NotNull Editor editor, @NotNull VirtualFile file) {
    int currentLine = editor.getCaretModel().getLogicalPosition().line;
    Document document = editor.getDocument();
    UpToDateLineNumberProvider lineNumberProvider = new UpToDateLineNumberProviderImpl(document, project);
    if (lineNumberProvider.isLineChanged(currentLine)) {
      return null;
    } else {
      int correctedLine = lineNumberProvider.getLineNumber(currentLine);
      FileAnnotation annotation = getAnnotation(document, file);
      if (annotation != null) {
        return LensLineBlame.create(annotation, correctedLine);
      }
    }
    return null;
  }

  @Nullable
  private FileAnnotation getAnnotation(@NotNull Document document, @NotNull VirtualFile file) {
    CachedAnnotation cachedAnnotation = getCachedAnnotation(document, file);
    if (cachedAnnotation != null) {
      if (cachedAnnotation.modificationStamp != document.getModificationStamp()) {
        annotationCache.invalidate(document);
        cachedAnnotation = getCachedAnnotation(document, file);
      }
    }
    if (cachedAnnotation != null) {
      return cachedAnnotation.annotation;
    }
    return null;
  }

  @Nullable
  private CachedAnnotation getCachedAnnotation(@NotNull Document document, @NotNull VirtualFile file) {
    try {
      return annotationCache.get(document, () -> annotationTimer.time(() -> {
        FileAnnotation annotation = loadAnnotation(file);
        return new CachedAnnotation(document.getModificationStamp(), annotation);
      }));
    } catch (Exception e) {
      log.warn("Failed to get cached annotation for " + file, e);
    }
    return null;
  }

  @Nullable
  private FileAnnotation loadAnnotation(@NotNull VirtualFile file) {
    try {
      return getGit().getAnnotationProvider().annotate(file);
    } catch (VcsException e) {
      log.warn("Failed to annotate " + file, e);
    }
    return null;
  }

  @Override
  public void fileClosed(@NotNull VirtualFile file) {
  }

  private static final class CachedAnnotation {
    private final long modificationStamp;
    private final FileAnnotation annotation;

    private CachedAnnotation(long modificationStamp, FileAnnotation annotation) {
      this.modificationStamp = modificationStamp;
      this.annotation = annotation;
    }
  }
}
