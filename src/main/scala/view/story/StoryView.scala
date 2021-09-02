package view.story

import controller.game.subcontroller.StoryController
import model.nodes.Pathway
import view.AbstractView
import view.util.common.ControlsPanel
import view.util.scalaQuestSwingComponents.SqSwingButton.SqSwingButton
import view.util.scalaQuestSwingComponents.SqSwingDialog.SqSwingDialog

import java.awt.BorderLayout
import java.awt.event.ActionEvent

/**
 * Represents the GUI for the navigation between [[model.nodes.StoryNode]]
 */
trait StoryView extends AbstractView {

  /**
   * Allows the narrative to be rendered.
   *
   * @param narrative the narrative to display.
   */
  def setNarrative(narrative: String): Unit

  /**
   * Allows rendering of all of the possible pathways.
   *
   * @param pathways the pathways to render.
   */
  def setPathways(pathways: Set[Pathway]): Unit
}

object StoryView {

  private class StoryViewSwing(private val storyController: StoryController) extends StoryView {
    private var _narrative: String = ""
    private var _pathways: Seq[Pathway] = Seq()

    this.setLayout(new BorderLayout())

    override def setNarrative(narrative: String): Unit = _narrative = narrative

    override def setPathways(pathways: Set[Pathway]): Unit = _pathways = pathways.toSeq

    override def populateView(): Unit = {
      this.add(
        ControlsPanel(
          List(
            ("s", ("[S] Status", _ => storyController.goToStatStatus())),
            ("h", ("[H] History", _ => storyController.goToHistory())),
            ("p", ("[P] Save Progress", _ => storyController.goToProgressSaver())),
            ("i", ("[I] Inventory", _ => storyController.goToInventory())),
            ("q", ("[Q] Quit", _ => {
              SqSwingDialog("Exit Confirm", "Do you really want to exit the game?",
                List(new SqSwingButton("yes", (_: ActionEvent) => storyController.close(), true),
                  new SqSwingButton("no", (_: ActionEvent) => {}, true)))
            })))),
        BorderLayout.NORTH
      )
      this.add(NarrativePanel(_narrative), BorderLayout.CENTER)
      this.add(PathwaysPanel(_pathways, p => storyController.choosePathWay(p)), BorderLayout.SOUTH)
    }
  }

  def apply(storyController: StoryController): StoryView = new StoryViewSwing(storyController)
}
