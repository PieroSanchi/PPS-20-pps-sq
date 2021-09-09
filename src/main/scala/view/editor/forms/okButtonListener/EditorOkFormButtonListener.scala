package view.editor.forms.okButtonListener
import controller.editor.EditorController
import view.form.{Form, OkFormButtonListener}

abstract class EditorOkFormButtonListener(override val form: Form, override val editorController: EditorController)
  extends OkFormButtonListener(form, editorController){

  override def performAction(): Unit = {
    editorControllerAction()
    editorController.execute()
  }

  def editorControllerAction(): Unit

}
