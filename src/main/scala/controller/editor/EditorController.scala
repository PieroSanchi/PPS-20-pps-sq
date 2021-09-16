package controller.editor

import controller.editor.graph.GraphBuilder
import controller.editor.graph.util.{ElementLabel, ElementStyle, StringUtils}
import controller.util.serialization.StoryNodeSerializer
import controller.{ApplicationController, Controller}
import model.StoryModel
import model.characters.Enemy
import model.items.KeyItem
import model.nodes.StoryNode.MutableStoryNode
import model.nodes.{Event, ItemEvent, MutablePathway, StoryNode}
import org.graphstream.ui.view.Viewer
import view.editor.EditorView

trait EditorController extends Controller {

  /**
   * Saves the current nodes structure serializing said structure in the provided path.
   * @param path where to serialize the nodes structure
   */
  def save(path: String): Unit

  /**
   * Switches the visibility of the nodes narrative in the graph view
   */
  def switchNodesNarrativeVisibility(): Unit

  /**
   * Switches the visibility of the pathways description in the graph view
   */
  def switchPathwaysDescriptionVisibility(): Unit

  /**
   * @param id the target node's id
   * @return the MutableStoryNode associated with the id (if present)
   * @see [[model.nodes.StoryNode.MutableStoryNode]]
   */
  def getStoryNode(id: Int): Option[MutableStoryNode]

  /**
   * @param startNodeId the id of the node that is at the begin of the pathway
   * @param endNodeId the id of the node that is at the end of the pathway
   * @return the MutablePathway that connects the two nodes
   * @see [[model.nodes.StoryNode.MutableStoryNode]]
   * @see [[model.nodes.MutablePathway]]
   */
  def getPathway(startNodeId: Int, endNodeId: Int): Option[MutablePathway]

  /**
   * Creates a new node in the story.
   * @param startingPathwayId the id of the node that originates the pathway leading to the new node
   * @param pathwayDescription the description of the pathway leading to the new node
   * @param newNodeNarrative the narrative of the new node
   * @return if the operation was successful
   */
  def addNewStoryNode(startingPathwayId: Int, pathwayDescription: String, newNodeNarrative: String): Boolean

  /**
   * Changes an existing node properties.
   * @param id the target node to edit
   * @param nodeNarrative the new narrative in the target node
   * @return if the operation was successful
   */
  def editExistingStoryNode(id: Int, nodeNarrative: String): Boolean

  /**
   * Deletes an existing node.
   * @param id the id of the target node
   * @return if the operation was successful
   */
  def deleteExistingStoryNode(id: Int): Boolean

  /**
   * Creates a new pathway to connect two nodes.
   * @param startNodeId the id of the node that is at the begin of the pathway
   * @param endNodeId the id of the node that is at the end of the pathway
   * @param pathwayDescription the description of the new pathway
   * @return if the operation was successful
   */
  def addNewPathway(startNodeId: Int, endNodeId: Int, pathwayDescription: String): Boolean

  /**
   * Changes an existing pathway properties.
   * @param startNodeId the id of the node that is at the begin of the pathway
   * @param endNodeId the id of the node that is at the end of the pathway
   * @param pathwayDescription the new description of the target pathway
   * @return if the operation was successful
   */
  def editExistingPathway(startNodeId: Int, endNodeId: Int, pathwayDescription: String): Boolean

  /**
   * Deletes an existing pathway.
   * @param startNodeId the id of the node that is at the begin of the pathway
   * @param endNodeId the id of the node that is at the end of the pathway
   * @return if the operation was successful
   */
  def deleteExistingPathway(startNodeId: Int, endNodeId: Int): Boolean

  /**
   * @param startNodeId the id of the node that is at the begin of a hypothetical new pathway
   * @param endNodeId the id of the node that is at the end of a hypothetical new pathway
   * @return if a hypothetical new pathway is valid
   */
  def isNewPathwayValid(startNodeId: Int, endNodeId: Int): Boolean

  def addEventToNode(nodeId: Int, event: Event): Boolean

  def getNodesIds(filter: StoryNode => Boolean): List[Int]

  def deleteEventFromNode(nodeId: Int, event: Event): Boolean

  def addEnemyToNode(nodeId: Int, enemy: Enemy): Boolean

  def deleteEnemyFromNode(nodeId: Int): Boolean

  def addPrerequisiteToPathway(originNodeId: Int, destinationNodeId: Int, prerequisite: StoryModel => Boolean): Boolean

  def deletePrerequisiteFromPathway(originNodeId: Int, destinationNodeId: Int): Boolean

  def getAllKeyItemsBeforeNode(targetNode: MutableStoryNode): List[KeyItem]
}

object EditorController {

  System.setProperty("org.graphstream.ui", "swing")

  private class EditorControllerImpl(routeNode: StoryNode) extends EditorController {

    private var printNodeNarrative: Boolean = false
    private var printEdgeLabel: Boolean = false

    private val editorView: EditorView = EditorView(this)
    private var nodes: (MutableStoryNode, Set[MutableStoryNode]) = StoryNodeConverter.fromImmutableToMutable(routeNode)
    private val graph = GraphBuilder.build(nodes._1)

    val graphViewer: Viewer = graph.display()
    decorateGraphGUI()

    override def execute(): Unit = editorView.render()

    override def close(): Unit = {
      graphViewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER)
      graphViewer.close()
      ApplicationController.execute()
    }

    override def save(path: String): Unit =
      StoryNodeSerializer.serializeStory(StoryNodeConverter.fromMutableToImmutable(nodes._1)._1, path)

    override def switchNodesNarrativeVisibility(): Unit = {
      printNodeNarrative = !printNodeNarrative
      decorateGraphGUI()
    }

    override def switchPathwaysDescriptionVisibility(): Unit = {
      printEdgeLabel = !printEdgeLabel
      decorateGraphGUI()
    }

    override def getStoryNode(id: Int): Option[MutableStoryNode] = nodes._2.find(n => n.id == id)

    override def getPathway(startNodeId: Int, endNodeId: Int): Option[MutablePathway] =
      getStoryNode(startNodeId) match {
        case None => None
        case _ => getStoryNode(startNodeId).get.mutablePathways.find(p => p.destinationNode.id == endNodeId)
      }

    override def addNewStoryNode(startNodeId: Int, pathwayDescription: String, newNodeNarrative: String): Boolean =
      if (getStoryNode(startNodeId).isEmpty || pathwayDescription.trim.isEmpty || newNodeNarrative.trim.isEmpty) {
        false
      } else {
        val newId: Int = nodes._2.maxBy(n => n.id).id + 1
        val newNode = MutableStoryNode(newId, newNodeNarrative, None, Set(), List())
        nodes = (nodes._1, nodes._2 + newNode)
        graph.addNode(newId.toString)
        addNewPathway(startNodeId, newId, pathwayDescription) //decorateGraphGUI called here
      }

    override def editExistingStoryNode(id: Int, nodeNarrative: String): Boolean = {
      val targetNode = getStoryNode(id)
      if (targetNode.isEmpty || nodeNarrative.trim.isEmpty){
        false
      } else {
        targetNode.get.narrative = nodeNarrative
        decorateGraphGUI()
        true
      }
    }

    override def deleteExistingStoryNode(id: Int): Boolean = {
      val targetNode: Option[MutableStoryNode] = getStoryNode(id)
      if (targetNode.isEmpty || targetNode.get == nodes._1){
        false
      } else {
        //removing all pathways leading to the target node
        nodes._2.filter(n => n.mutablePathways.exists(p => p.destinationNode.id == id))
          .foreach(n => n.mutablePathways = n.mutablePathways.filter(p => p.destinationNode.id != id))
        //recreating structure implicitly deleting unreachable nodes
        nodes = StoryNodeConverter.fromImmutableToMutable(nodes._1)
        //removing the target node from the GUI graph
        graph.removeNode(id.toString)
        //removing all other deleted nodes from the GUI graph
        var removedNodesId: Set[Int] = Set()
        graph.nodes().mapToInt(n => n.getId.toInt)
          .filter(id => !nodes._2.exists(sn => sn.id == id))
          .forEach(id => removedNodesId = removedNodesId + id) //removing nodes here throws null pointer
        removedNodesId.foreach(id => {
          if (graph.nodes.anyMatch(n => n.getId.toInt == id)) { graph.removeNode(id.toString)}
        })
        decorateGraphGUI()
        true
      }
    }

    override def addNewPathway(startNodeId: Int, endNodeId: Int, pathwayDescription: String): Boolean = {
      val startNode: Option[MutableStoryNode] = getStoryNode(startNodeId)
      val endNode: Option[MutableStoryNode] = getStoryNode(endNodeId)
      if (startNode.isEmpty || endNode.isEmpty || pathwayDescription.trim.isEmpty){
        false
      } else {
        startNode.get.mutablePathways =
          startNode.get.mutablePathways + MutablePathway(pathwayDescription, endNode.get, None)
        graph.addEdge(
          startNodeId + StringUtils.pathwayIdSeparator + endNodeId,
          startNodeId.toString,
          endNodeId.toString
        )
        decorateGraphGUI()
        true
      }
    }

    override def editExistingPathway(startNodeId: Int, endNodeId: Int, pathwayDescription: String): Boolean = {
      if (
        getStoryNode(startNodeId).nonEmpty &&
          getStoryNode(endNodeId).nonEmpty &&
          pathwayDescription.trim.nonEmpty &&
          getPathway(startNodeId, endNodeId).nonEmpty
      ){
        getPathway(startNodeId, endNodeId).get.description = pathwayDescription
        decorateGraphGUI()
        true
      } else {
        false
      }
    }

    override def deleteExistingPathway(startNodeId: Int, endNodeId: Int): Boolean = {
      val startNode = getStoryNode(startNodeId)
      if (startNode.isEmpty || getStoryNode(endNodeId).isEmpty){
        false
      } else {
        startNode.get.mutablePathways = startNode.get.mutablePathways.filter(p => p.destinationNode.id != endNodeId)
        graph.removeEdge(startNodeId + StringUtils.pathwayIdSeparator + endNodeId)
        decorateGraphGUI()
        true
      }
    }

    override def isNewPathwayValid(startNodeId: Int, endNodeId: Int): Boolean = {

      def searchForDestination(searchedNode: MutableStoryNode, currentNode: MutableStoryNode): Boolean =
        currentNode != searchedNode && currentNode.mutablePathways.forall(
          p => searchForDestination(searchedNode, p.destinationNode)
        )

      val startNode = getStoryNode(startNodeId)
      val endNode = getStoryNode(endNodeId)
      if (startNode.isEmpty ||
        endNode.isEmpty ||
        /* cannot create two pathways with same origin and destination */
        startNode.get.mutablePathways.exists(p => p.destinationNode == endNode.get)
      ){
        false
      } else {
        //searching if, from the end node, the start node is unreachable (preventing a loop)
        searchForDestination(startNode.get, endNode.get)
      }
    }

    override def addEventToNode(nodeId: Int, event: Event): Boolean = {
      val node = getStoryNode(nodeId)
      if(node.isEmpty){
        false
      } else {
        node.get.events = node.get.events :+ event
        decorateGraphGUI()
        true
      }
    }

    override def deleteEventFromNode(nodeId: Int, event: Event): Boolean = {
      val node = getStoryNode(nodeId)
      if(node.isEmpty || !node.get.events.contains(event)){
        false
      } else {
        node.get.events = node.get.events.filter(e => e != event)
        decorateGraphGUI()
        true
      }
    }

    override def addEnemyToNode(nodeId: Int, enemy: Enemy): Boolean = {
      val node = getStoryNode(nodeId)
      if(node.isEmpty || node.get.enemy.nonEmpty){
        false
      } else {
        node.get.enemy = Some(enemy)
        decorateGraphGUI()
        true
      }
    }

    override def deleteEnemyFromNode(nodeId: Int): Boolean = {
      val node = getStoryNode(nodeId)
      if(node.isEmpty || node.get.enemy.isEmpty){
        false
      } else {
        node.get.enemy = None
        decorateGraphGUI()
        true
      }
    }

    override def addPrerequisiteToPathway(originNodeId: Int,
                                          destinationNodeId: Int,
                                          prerequisite: StoryModel => Boolean): Boolean = {
      val pathway = getPathway(originNodeId, destinationNodeId)
      if(pathway.isEmpty || pathway.get.prerequisite.nonEmpty){
        false
      } else {
        pathway.get.prerequisite = Some(prerequisite)
        decorateGraphGUI()
        true
      }
    }

    override def deletePrerequisiteFromPathway(originNodeId: Int, destinationNodeId: Int): Boolean = {
      val pathway = getPathway(originNodeId, destinationNodeId)
      if(pathway.isEmpty || pathway.get.prerequisite.isEmpty){
        false
      } else {
        pathway.get.prerequisite = None
        decorateGraphGUI()
        true
      }
    }

    override def getAllKeyItemsBeforeNode(targetNode: MutableStoryNode): List[KeyItem] = {

      def getPredecessors(node: MutableStoryNode): Set[MutableStoryNode] =
        nodes._2.filter(n => n.pathways.exists(p => p.destinationNode == node))

      def stepBack(node: MutableStoryNode,
                   visitedNodes: Set[MutableStoryNode]): (List[KeyItem], Set[MutableStoryNode]) = {
        var keyItems: List[KeyItem] = List()
        var visitedNodesVar: Set[MutableStoryNode] = visitedNodes + node //adding this node to the already visited

        //getting all key items in this node
        node.events.foreach {
          case itemEvent: ItemEvent => itemEvent.item match {
            case keyItem: KeyItem => keyItems = keyItems :+ keyItem
          }
        }

        //for each predecessor of this node
        getPredecessors(node).foreach(n => {
          //only if the predecessor hasn't been visited yet
          if(!visitedNodes.contains(n)){
            val nodeRes = stepBack(n, visitedNodesVar)
            keyItems = keyItems ++ nodeRes._1 //adding the key items found exploring this predecessor
            visitedNodesVar = visitedNodesVar ++ nodeRes._2 //adding the visited nodes exploring the predecessor
          }
        })

        //returning the tuple
        (keyItems, visitedNodes)
      }

      stepBack(targetNode, Set())._1
    }

    private def decorateGraphGUI(): Unit = {
      graph.nodes().forEach(n => {
        if(n.getId != nodes._1.id.toString) {
          setupNonRouteNode(n.getId)
        } else {
          setupRouteNode()
        }
      })
      graph.edges().forEach(e => setupEdge(
        e.getId.split(StringUtils.pathwayIdSeparator)(0),
        e.getId.split(StringUtils.pathwayIdSeparator)(1))
      )
    }

    private def setupRouteNode(): Unit = {
      ElementStyle.decorateRouteNode(
        graph.getNode(nodes._1.id.toString),
        nodes._1.events.nonEmpty,
        nodes._1.enemy.nonEmpty
      )
      ElementLabel.putLabelOnElement(graph.getNode(nodes._1.id.toString), printNodeNarrative)(
        StringUtils.truncateString(StringUtils.buildLabel(nodes._1.id.toString, nodes._1.narrative)),
        nodes._1.id.toString
      )
    }

    private def setupNonRouteNode(nodeId: String): Unit = {
      val mutableStoryNode = nodes._2.find(n => n.id.toString == nodeId).get
      ElementStyle.decorateNode(
        graph.getNode(nodeId),
        mutableStoryNode.events.nonEmpty,
        mutableStoryNode.enemy.nonEmpty,
        mutableStoryNode.pathways.isEmpty
      )
      ElementLabel.putLabelOnElement(graph.getNode(nodeId), printNodeNarrative)(
        StringUtils.truncateString(StringUtils.buildLabel(mutableStoryNode.id.toString, mutableStoryNode.narrative)),
        mutableStoryNode.id.toString
      )
    }

    private def setupEdge(startNodeId: String, endNodeId: String): Unit = {
      val startNode = nodes._2.find(n => n.id.toString == startNodeId).get
      ElementStyle.decorateEdge(
        graph.getEdge(startNodeId + StringUtils.pathwayIdSeparator + endNodeId),
        startNode.pathways.find(p => p.destinationNode.id.toString == endNodeId).get.prerequisite.nonEmpty
      )
      ElementLabel.putLabelOnElement(
        graph.getEdge(startNodeId + StringUtils.pathwayIdSeparator + endNodeId), printEdgeLabel
      )(
        StringUtils.buildLabel(
          startNodeId + StringUtils.pathwayIdSeparator + endNodeId,
          startNode.pathways.find(p => p.destinationNode.id.toString == endNodeId).get.description
        ),
        startNodeId + StringUtils.pathwayIdSeparator + endNodeId
      )
    }

    override def getNodesIds(filter: StoryNode => Boolean): List[Int] =
      nodes._2.filter(n => filter(n)).map(n => n.id).toList.sortWith((i, j) => i < j)

  }

  def apply(routeNode: StoryNode): EditorController = new EditorControllerImpl(routeNode)
}
