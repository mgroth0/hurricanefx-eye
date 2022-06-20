modtype = LIB

apis(
  libs.fx.base,
  libs.kotlinx.serialization.json,
  projects.k.json
)

implementations(
  projects.k.reflect,
  projects.k.async,
  projects.k.kjlib
)

plugins {
  kotlin("plugin.serialization")
}