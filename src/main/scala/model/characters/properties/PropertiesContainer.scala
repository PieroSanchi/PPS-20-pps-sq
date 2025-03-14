package model.characters.properties

import model.characters.properties.Health.Health
import model.characters.properties.stats.StatName.StatName
import model.characters.properties.stats.{Stat, StatModifier}

/**
 * A propertiesContainer contains all the properties of a specific character.
 */
sealed trait PropertiesContainer extends Serializable {

  def health: Health

  def stats: Set[Stat]

  /**
   * Returns a stat object by it's statName.
   *
   * @param statName the name of the stat.
   * @return the relative stat.
   */
  def stat(statName: StatName): Stat

  /**
   * Returns every statModifier that alters a specific stat.
   *
   * @param statName the statName of the stat.
   * @return a set of every modifier of statName.
   */
  def statModifiers(statName: StatName): Set[StatModifier]

  def statModifiers: Set[StatModifier]

  def statModifiers_=(statModifierSet: Set[StatModifier]): Unit

}

object PropertiesContainer {

  /**
   * Implementation of PropertiesContainer.
   *
   * @param maxPS the number of PS for character's full life.
   * @param stats the statistics of a specific Character.
   */
  private class PropertiesContainerImpl(private val maxPS: Int, val stats: Set[Stat]) extends PropertiesContainer {

    val health: Health = Health(maxPS)
    var statModifiers: Set[StatModifier] = Set()

    override def stat(statName: StatName): Stat = stats.find(s => s.statName == statName).get

    override def statModifiers(st: StatName): Set[StatModifier] = statModifiers.filter(s => s.statName == st)

  }

  def apply(maxPS: Int, stats: Set[Stat]): PropertiesContainer = new PropertiesContainerImpl(maxPS, stats)

}
