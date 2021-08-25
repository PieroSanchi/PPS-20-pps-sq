package model.nodes

import model.StoryModel
import model.characters.properties.stats.StatModifier

/**
 * Contains the strategy to pass to StoryNode's events.
 * @see [[model.nodes.StoryNode]]
 */
sealed trait Event {
  /**
   * Strategy of what happens in a StoryNode's event.
   * @param storyModel the StoryModel to manipulate on execution
   * @see [[model.StoryModel]]
   * @see [[model.nodes.StoryNode]]
   */
  def execute(storyModel: StoryModel): Unit
}

object StatEvent {
  class StatEvent(statModifier: StatModifier) extends Event {
    override def execute(storyModel: StoryModel): Unit =
      storyModel.player.properties.statModifiers = storyModel.player.properties.statModifiers + statModifier
  }

  def apply(statModifier: StatModifier): StatEvent = new StatEvent(statModifier)
}
