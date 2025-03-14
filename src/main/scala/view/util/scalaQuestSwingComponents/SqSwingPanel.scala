package view.util.scalaQuestSwingComponents

import java.awt.{BorderLayout, Color, FlowLayout, GridBagLayout, GridLayout}
import javax.swing.{BoxLayout, JPanel}

abstract class SqSwingPanel() extends JPanel {
  this.setBackground(Color.BLACK)
}

abstract class SqSwingFlowPanel() extends SqSwingPanel {
  this.setLayout(new FlowLayout)
}

abstract class SqSwingBoxPanel(axis: Int) extends SqSwingPanel {
  this.setLayout(new BoxLayout(this, axis))
}

abstract class SqSwingGridPanel(rows: Int, cols: Int) extends SqSwingPanel {
  this.setLayout(new GridLayout(rows, cols))
}

abstract class SqSwingBorderPanel() extends SqSwingPanel {
  this.setLayout(new BorderLayout)
}

abstract class SqSwingGridBagPanel() extends SqSwingPanel {
  this.setLayout(new GridBagLayout)
}
