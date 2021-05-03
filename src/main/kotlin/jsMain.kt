import kotlinx.browser.document
import org.w3c.dom.*
import widget.Widget
import widget.WidgetFactory
import widget.WidgetHolder
import widget.afterRender

fun main() {
    val widgetHolder = WidgetHolder()
    val widgetFactory = WidgetFactory().apply {
        register("w-ButtonWidget") { ButtonWidget() }
    }

    var counter = 0
    fun MainWidget(): Widget {
        return Widget(
            """
               |<h2><span id="counter"></span> welcome!</h2>
               |<button id="new">new</button>
               |<button id="close">close</button> <br> <br>
               |<button id="newRow">new row</button>
               |<div id="rows"></div>
               |""".trimMargin()

        ).apply {
            val c = counter++
            val counter: HTMLSpanElement by this
            val close: HTMLButtonElement by this
            val new: HTMLButtonElement by this
            val newRow: HTMLButtonElement by this
            val rows: HTMLDivElement by this
            counter.innerHTML = "counter $c"
            close.onclick = { close() }
            new.onclick = { show(MainWidget()) }
            newRow.onclick = {
                RowWidget().also {
                    it.widgetFactory = widgetFactory
                    it.label.innerHTML = "label count: ${rows.childElementCount}"
                    it.checkbox.checked = rows.childElementCount % 2 == 0
                    rows.appendChild(it.container)
                }
            }
        }.also {
            it.widgetFactory = widgetFactory
        }
    }

    val loginWidget = Widget(
        """
            |username: <input type="text" id="user"> <br>
            |password: <input type="text" id="pass"> <br>
            |<br>
            |<button id="login">login</button> <br>
            |""".trimMargin()
    ).apply {
//            |<w-ButtonWidget></w-ButtonWidget>
//
        val login: HTMLButtonElement by this

        afterRender {
            login.onclick = { widgetHolder.show(MainWidget()) }
        }
    }
    document.body?.appendChild(widgetHolder.container)
    widgetHolder.show(loginWidget)
}


