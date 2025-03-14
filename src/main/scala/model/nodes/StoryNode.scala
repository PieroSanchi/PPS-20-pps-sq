package model.nodes

import model.StoryModel

/**
 * Trait that represents a story node, which is used to have a reference of all the current possible pathways and
 * of what is happening in the story.
 */
trait StoryNode extends Serializable{
  val id: Int
  val narrative: String
  def pathways: Set[Pathway]
  def events: List[StoryModel => Unit]
}

object StoryNode {

  /**
   * Implementation of StoryNode.
   * @param id is the unique id of the node.
   * @param narrative is the text that the player will read, which is the actual story.
   * @param pathways are the possible pathways that the player can see and choose, to progress in the story.
   * @param events an ordered list containing eventual events to handle while entering this node.
   * @return the story node.
   */
  def apply(id: Int, narrative: String, pathways: Set[Pathway], events: List[StoryModel => Unit]): StoryNode =
    new StoryNodeImpl(id, narrative, pathways, events)

  private class StoryNodeImpl(override val id: Int,
                              override val narrative: String,
                              override val pathways: Set[Pathway],
                              override val events: List[StoryModel => Unit]) extends StoryNode {
    require(
      !id.isNaN && narrative != null &&
        narrative.trim.nonEmpty &&
        pathways.forall(p => !pathways.exists(o => !o.eq(p) && o.destinationNode.eq(p.destinationNode))) &&
        containsOnePathwayWithNoCondition(pathways) &&
        events != null
    )

    private def containsOnePathwayWithNoCondition(pathways: Set[Pathway]): Boolean =
      if (pathways.nonEmpty) pathways.exists(p => p.prerequisite.isEmpty) else true
  }
}