package edu.cap10.cora

trait Age extends TimeSensitive {

  private[this] var _age = 0
  def age = _age

  override def _tick(when:Int) = {
    _age += 1
    super._tick(when)
  }

}
