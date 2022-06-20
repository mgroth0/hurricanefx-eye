modtype = LIB

apis(
  libs.fx.base,
  libs.kotlinx.serialization.json,
  projects.kj.json
)

implementations(
  projects.kj.reflect,
  projects.k.async,
  projects.kj.kjlib
)

plugins {
  kotlin("plugin.serialization")
}