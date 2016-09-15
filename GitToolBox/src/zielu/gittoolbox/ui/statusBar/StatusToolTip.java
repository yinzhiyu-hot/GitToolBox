package zielu.gittoolbox.ui.statusBar;

import com.intellij.openapi.project.Project;
import com.intellij.util.text.DateFormatUtil;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.Collection;
import java.util.TreeMap;
import java.util.stream.Collectors;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxConfigForProject;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.StatusText;
import zielu.gittoolbox.util.Html;

public class StatusToolTip {
    private final Project myProject;
    private GitRepository myCurrentRepository;
    private GitAheadBehindCount myCurrentAheadBehind;

    private String myCurrentText;

    public StatusToolTip(@NotNull Project project) {
        myProject = project;
    }

    @Nullable
    public String getText() {
        if (myCurrentText == null) {
            myCurrentText = prepateToolTip();
        }
        return myCurrentText;
    }

    private String prepateToolTip() {
        if (myCurrentAheadBehind == null) {
            return prepareInfoToolTipPart().toString();
        } else {
            StringBand infoPart = prepareInfoToolTipPart();
            if (infoPart.length() > 0) {
                infoPart.append(Html.br);
            }
            Collection<GitRepository> repositories = GitUtil.getRepositories(myProject);
            if (repositories.size() == 1) {
                return infoPart.append(StatusText.formatToolTip(myCurrentAheadBehind)).toString();
            } else if (repositories.size() > 2) {
                PerRepoInfoCache cache = GitToolBoxProject.getInstance(myProject).perRepoStatusCache();
                TreeMap<String, String> statuses = new TreeMap<>();
                String currentRepoKey = null;
                for (GitRepository repository : repositories) {
                    GitAheadBehindCount count = cache.getInfo(repository).count;
                    if (count != null) {
                        String name = repository.getRoot().getName();
                        String statusText = StatusText.format(count);
                        if (repository.equals(myCurrentRepository)) {
                            currentRepoKey = name;
                        }
                        statuses.put(name, statusText);
                    }
                }
                if (!statuses.isEmpty()) {
                    if (infoPart.length() > 0) {
                        infoPart.append(Html.hr);
                    }
                    final String currentName = currentRepoKey;
                    infoPart.append(
                        statuses.entrySet().stream().map(e -> {
                            String repoStatus = GitUIUtil.bold(e.getKey()) + ": " + e.getValue();
                            if (e.getKey().equals(currentName)) {
                                repoStatus = Html.u(repoStatus);
                            }
                            return repoStatus;
                        }).collect(Collectors.joining(Html.br))
                    );
                }
            }
            return infoPart.toString();
        }
    }

    private StringBand prepareInfoToolTipPart() {
        GitToolBoxConfigForProject config = GitToolBoxConfigForProject.getInstance(myProject);
        StringBand result = new StringBand();
        if (config.autoFetch) {
            result.append(GitUIUtil.bold(ResBundle.getString("message.autoFetch"))).append(": ");
            long lastAutoFetch = GitToolBoxProject.getInstance(myProject).autoFetch().lastAutoFetch();
            if (lastAutoFetch != 0) {
                result.append(DateFormatUtil.formatBetweenDates(lastAutoFetch, System.currentTimeMillis()));
            } else {
                result.append(ResBundle.getString("common.on"));
            }
        }

        return result;
    }


    public void update(@NotNull GitRepository repository, @Nullable GitAheadBehindCount aheadBehind) {
        myCurrentRepository = repository;
        myCurrentAheadBehind = aheadBehind;
        myCurrentText = null;
    }

    public void clear() {
        myCurrentRepository = null;
        myCurrentAheadBehind = null;
        myCurrentText = null;
    }
}
