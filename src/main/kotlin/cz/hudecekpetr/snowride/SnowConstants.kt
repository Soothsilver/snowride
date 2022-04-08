package cz.hudecekpetr.snowride

import javafx.stage.Stage

object SnowConstants {
    lateinit var primaryStage: Stage

    fun setStageTitle(suffix: String = "") {
        primaryStage.title = "Snowride $suffix"
    }
}
