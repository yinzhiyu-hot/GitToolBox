package zielu.gittoolbox.ui.config.app.v2

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import com.intellij.ui.treeStructure.Tree
import zielu.gittoolbox.ResBundle.message
import zielu.gittoolbox.ResIcons
import zielu.gittoolbox.config.GitToolBoxConfigPrj
import zielu.gittoolbox.ui.config.common.AutoFetchExclusionsTreeModel
import zielu.intellij.ui.GtFormUiEx
import javax.swing.JComponent
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.TreeSelectionModel

internal class AutoFetchExclusionsForm : GtFormUiEx<GitToolBoxConfigPrj> {
  private val autoFetchExclusionsModel = AutoFetchExclusionsTreeModel()
  private val autoFetchExclusions = Tree(autoFetchExclusionsModel)

  private lateinit var decorator: ToolbarDecorator
  private lateinit var addRemoteButton: AnActionButton
  private lateinit var removeRemoteButton: AnActionButton
  private lateinit var panel: DialogPanel

  override fun init() {
    autoFetchExclusions.isRootVisible = false
    autoFetchExclusions.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

    decorator = ToolbarDecorator.createDecorator(autoFetchExclusions)
    decorator.setAddActionName(message("configurable.prj.autoFetch.exclusions.add.label"))
    decorator.setRemoveActionName(message("configurable.prj.autoFetch.exclusions.remove.label"))
    addRemoteButton = object : AnActionButton("Add remote", ResIcons.Plus) {
      override fun actionPerformed(e: AnActionEvent) {
        //TODO:
      }
    }
    removeRemoteButton = object : AnActionButton("Remove remote", ResIcons.Minus) {
      override fun actionPerformed(e: AnActionEvent) {
        //TODO:
      }
    }
    decorator.addExtraActions(addRemoteButton, removeRemoteButton)
    autoFetchExclusions.addTreeSelectionListener(this::onTreeSelection)

    val decoratorPanel = decorator.createPanel()

    panel = panel(LCFlags.fillX) {
      row {
        decoratorPanel(CCFlags.growX)
      }
    }
  }

  private fun onTreeSelection(event: TreeSelectionEvent) {

  }

  override fun getContent(): JComponent = panel

  override fun afterStateSet() {
    //TODO:
  }

  override fun fillFromState(state: GitToolBoxConfigPrj) {
    autoFetchExclusionsModel.setConfigs(state.autoFetchExclusionConfigs.map { it.copy() })
  }

  override fun isModified(): Boolean {
    //TODO:
    return false
  }

  override fun applyToState(state: GitToolBoxConfigPrj) {
    //TODO:
  }
}
