package controller.prolog.structs

import alice.tuprolog.Var
import controller.prolog
import controller.prolog.SqPrologEngine
import controller.prolog.structs.StructUtil.areAllSolutionPresent
import model.nodes.{Pathway, StoryNode}
import specs.FlatTestSpec
import org.scalatest.DoNotDiscover
import controller.prolog.util.PrologImplicits._

/**
 * Tested in [[suites.PrologEngineSuite]].
 */
@DoNotDiscover
class StoryWalkthroughStructTest extends FlatTestSpec {

  val secondDestinationNode: StoryNode = StoryNode(3, "narrative(3)", None, Set.empty, List())
  val secondDestinationPathway: Pathway = Pathway("description(1-3)", secondDestinationNode, None)
  val firstDestinationNode: StoryNode = StoryNode(2, "narrative(2)", None, Set.empty, List())
  val firstDestinationPathway: Pathway = Pathway("description(1-2)", firstDestinationNode, None)
  val middleNode: StoryNode = StoryNode(
    1, "narrative(1)", None,
    Set(firstDestinationPathway, secondDestinationPathway),
    List()
  )
  val middlePathway: Pathway = Pathway("description(0-1)", middleNode, None)
  val startingNode: StoryNode = StoryNode(0, "narrative(0)", None, Set(middlePathway), List())

  var engine: SqPrologEngine = prolog.SqPrologEngine(startingNode)

  "The Prolog Engine" should "calculate correctly the stories walkthrough" in {
    val solutions = engine.resolve(StoryWalkthroughStruct(0, new Var()))
    solutions.size shouldEqual 2
    areAllSolutionPresent[String](
      Set(
        Seq("narrative(0)", "description(0-1)", "narrative(1)", "description(1-2)", "narrative(2)"),
        Seq("narrative(0)", "description(0-1)", "narrative(1)", "description(1-3)", "narrative(3)")
      ),
      solutions.head.walkthrough,
      solutions(1).walkthrough) shouldBe true
  }
}
