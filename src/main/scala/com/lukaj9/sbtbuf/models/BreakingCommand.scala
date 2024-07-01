package com.lukaj9.sbtbuf.models

case class BreakingCommand(
    against: AgainstBreaking
)

case class AgainstBreaking(
    value: String
)
