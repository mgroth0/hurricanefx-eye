modtype = LIB

apis(
  libs.fx.base,
  libs.kotlinx.serialization.json,
  projects.kj.json
)

implementations(
  projects.kj.reflect
)

plugins {
  kotlin("plugin.serialization")
}