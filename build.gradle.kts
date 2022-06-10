modtype = LIB

apis(
  libs.fx.base,
  libs.kotlinx.serialization.json,
  projects.kj.json
)

implementations(
  projects.kj.reflect,
  projects.kj.async
)

plugins {
  kotlin("plugin.serialization")
}