package view.util.scalaQuestSwingComponents

import java.awt.event.ActionListener
import javax.swing.JButton

object SqSwingButton {

  class SqSwingButton(text: String, action: ActionListener, enabled: Boolean) extends JButton {
    this.setText(text)
    this.addActionListener(action)
    this.setEnabled(enabled)
  }

  def apply(text: String, action: ActionListener): SqSwingButton = new SqSwingButton(text, action, true)

  def apply(text: String, action: ActionListener, enabled: Boolean): SqSwingButton =
    new SqSwingButton(text, action, enabled)
}
