package ru.neoflex.utility

sealed trait LogLevel {
  def level: String
}

case object WARNING extends LogLevel {
  override def level: String = "WARN"
}

case object INFO extends LogLevel {
  override def level: String = "INFO"
}
