package model.nodes.util

import model.nodes.{Pathway, StoryNode}

import scala.util.Random
import scala.collection.mutable.{Set => MutableSet}

/**
 * Component used to create a Random [[model.nodes.StoryNode]]
 */
object RandomStoryNodeGenerator {

  private object RandomStoryParams{
    val MaxNodesInLayer = 5
    val Layers = 7
  }

  import model.nodes.util.RandomStoryNodeGenerator.RandomStoryParams.{Layers, MaxNodesInLayer}

  private def rnd(max: Int): Int = Random.nextInt(max) + 1

  private def getMaxId(nodes: Seq[StoryNode]): Int = nodes.map(n => n.id).max

  def generate(): StoryNode = {

    val generateLastLayer = () =>
      for (x <- 0 until rnd(MaxNodesInLayer)) yield StoryNode(x, "final node " + x, Set.empty, List())

    def generateLayers(depth: Int): Seq[StoryNode] = depth match {
      case 0 => generateLastLayer()
      case _ => generateIntermediateLayer(depth)
    }

    def genPathwaysToNextLayerNode(node: StoryNode, nCurrentLayersNodes: Int): MutableSet[Pathway] =
      (for (_ <- 0 until Random.nextInt(nCurrentLayersNodes) + 1)
        yield Pathway("go to node " + node.id, node, None)).toSet.to[MutableSet]

    def genPathwaysToAllNextLayerNodes(nextLayer: Seq[StoryNode], nCurrentLayerNodes: Int): Seq[MutableSet[Pathway]] =
      for (node <- nextLayer) yield genPathwaysToNextLayerNode(node, nCurrentLayerNodes)

    def generateIntermediateLayer(depth: Int): Seq[StoryNode] = {
      val nextLayer = generateLayers(depth - 1)
      val startingId = getMaxId(nextLayer) + 1
      val nCurrentLayerNodes = rnd(MaxNodesInLayer)
      val pathwaysToNextNodes: Seq[MutableSet[Pathway]] = genPathwaysToAllNextLayerNodes(nextLayer, nCurrentLayerNodes)
      var res = Seq.empty[StoryNode]
      for (id <- startingId until startingId + nCurrentLayerNodes) {
        var newNodePathways = Seq.empty[Pathway]
        for (pathwaysToNode <- pathwaysToNextNodes) {
          if (pathwaysToNode.nonEmpty) {
            val assignedPathway = pathwaysToNode.last
            newNodePathways = newNodePathways union Seq(assignedPathway)
            pathwaysToNode.remove(assignedPathway)
          }
        }
        val narrative =
          if (newNodePathways.isEmpty) "final node " + id else "node " + id + ", max remaining layers " + depth
        res = res :+ StoryNode(id, narrative, newNodePathways.toSet, List())
      }
      res
    }
    val generated = generateLayers(Layers - 1)
    val pathways: Seq[Pathway] = for (node <- generated) yield Pathway("go to node " + node.id, node, None)
    StoryNode(getMaxId(generated) + 1, "starting node, max remaining layers " + Layers, pathways.toSet, List())
  }

}
