package example

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import widget.Widget
import widget.WidgetFactory

private var globalCounter = 0

class MainWidget(wf: WidgetFactory) : Widget(
    """
               |<h2><span id="counter"></span> welcome!</h2>
               |<button id="new">new</button>
               |<button id="close">close</button> <br> <br>
               |<button id="newRow">new row</button>
               |<div id="rows"></div>
               |""".trimMargin()

) {
    private val counter: HTMLSpanElement by this
    private val close: HTMLButtonElement by this
    private val new: HTMLButtonElement by this
    private val newRow: HTMLButtonElement by this
    private val rows: HTMLDivElement by this

    init {
        widgetFactory = wf
        setup()
    }

    private fun setup() {
        globalCounter += 1
        counter.innerHTML = "counter $globalCounter"
        close.onclick = { close() }
        new.onclick = { show(MainWidget(widgetFactory)) }
        newRow.onclick = {
            RowWidget().also {
                it.widgetFactory = widgetFactory
                it.label.innerHTML = "label count: ${rows.childElementCount}"
                it.checkbox.checked = rows.childElementCount % 2 == 0
                rows.appendChild(it.container)
            }
        }
    }
}
