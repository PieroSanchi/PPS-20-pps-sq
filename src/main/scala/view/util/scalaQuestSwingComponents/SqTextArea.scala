package view.util.scalaQuestSwingComponents

import java.awt.Color
import javax.swing.JTextArea

case class SqTextArea(content: String, editable: Boolean = false) extends JTextArea(content) {

  private val FontSize = 15

  this.setEditable(editable)
  this.setFocusable(false)
  this.setLineWrap(true)
  this.setWrapStyleWord(true)
  this.setBackground(Color.BLACK)
  this.setForeground(Color.WHITE)
  this.setFont(SqFont(bold = false, FontSize))
}
