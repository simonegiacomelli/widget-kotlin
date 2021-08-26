package example

import kotlinx.browser.document
import widget.WidgetFactory
import widget.WidgetHolder

fun main(){
    val widgetHolder = WidgetHolder()
    val widgetFactory = WidgetFactory().apply {
        register("w-example.ButtonWidget") { ButtonWidget() }
    }

    val loginWidget = LoginWidget().also { it.widgetFactory = widgetFactory }
    document.body?.appendChild(widgetHolder.container)
    widgetHolder.show(loginWidget)
}