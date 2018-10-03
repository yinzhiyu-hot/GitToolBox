package zielu.gittoolbox.ui.config.app;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxUpdateProjectApp;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.intellij.ui.GtConfigurableBase;

public class GtConfigurable extends GtConfigurableBase<GtForm, GitToolBoxConfig2>
    implements SearchableConfigurable {
  private final Logger log = Logger.getInstance(getClass());

  @Nls
  @Override
  public String getDisplayName() {
    return ResBundle.getString("configurable.app.displayName");
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Override
  protected GtForm createForm() {
    return new GtForm();
  }

  @Override
  protected GitToolBoxConfig2 getConfig() {
    return GitToolBoxConfig2.getInstance();
  }

  @Override
  protected void setFormState(GtForm form, GitToolBoxConfig2 config) {
    log.debug("Set form state");
    form.setPresenter(config.getPresenter());
    form.setShowGitStatus(config.showStatusWidget);
    form.setShowProjectViewStatus(config.showProjectViewStatus);
    form.setBehindTrackerEnabled(config.behindTracker);
    form.setUpdateProjectAction(GitToolBoxUpdateProjectApp.getInstance().getById(config.getUpdateProjectActionId()));
    form.setDecorationParts(config.decorationParts);
    form.setShowLensBlame(config.showLensBlame);
  }

  @Override
  protected boolean checkModified(GtForm form, GitToolBoxConfig2 config) {
    boolean modified = config.isPresenterChanged(form.getPresenter());
    modified = modified || config.isShowStatusWidgetChanged(form.getShowGitStatus());
    modified = modified || config.isShowProjectViewStatusChanged(form.getShowProjectViewStatus());
    modified = modified || config.isBehindTrackerChanged(form.getBehindTrackerEnabled());
    modified = modified || config.isUpdateProjectActionId(form.getUpdateProjectAction().getId());
    modified = modified || config.isDecorationPartsChanged(form.getDecorationParts());
    modified = modified || config.isShowLensBlameChanged(form.getShowLensBlame());
    log.debug("Modified: ", modified);
    return modified;
  }

  @Override
  protected void doApply(GtForm form, GitToolBoxConfig2 config) throws ConfigurationException {
    config.setPresenter(form.getPresenter());
    config.showStatusWidget = form.getShowGitStatus();
    config.showProjectViewStatus = form.getShowProjectViewStatus();
    config.decorationParts = form.getDecorationParts();
    config.behindTracker = form.getBehindTrackerEnabled();
    config.updateProjectActionId = form.getUpdateProjectAction().getId();
    config.decorationParts = form.getDecorationParts();
    config.showLensBlame = form.getShowLensBlame();

    //Mark migrated here to handle case when config is modified without opening a project
    //Example: from launch dialog
    config.previousVersionMigrated = true;

    config.fireChanged();
    log.debug("Applied");
  }

  @NotNull
  @Override
  public String getId() {
    return "zielu.gittoolbox.app.config";
  }

  @Nullable
  @Override
  public Runnable enableSearch(String option) {
    return null;
  }
}
