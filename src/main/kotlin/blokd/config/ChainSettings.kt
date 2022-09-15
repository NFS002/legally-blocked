package blokd.config

import java.io.FileInputStream
import java.util.Properties

object ChainSettings {

        val properties:Properties

        init {
            this.properties = loadProperties()
            this.setProperties(properties)
        }

        private fun setProperties(properties: Properties) {
                //TODO("something")
        }


        private fun loadProperties() : Properties {
            val props:Properties = Properties()
            val defaultConfigPath = "src/main/resources/settings.properties"
            val configPath = System.getProperty("BLOKD__SETTINGS_PATH", defaultConfigPath)
            FileInputStream(configPath).use { fis ->
                props.load(fis)
            }
            return props
        }
}