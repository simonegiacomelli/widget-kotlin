package widget

import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.asList
import org.w3c.dom.get
import kotlin.reflect.KProperty

open class Widget(val html: String) {
    lateinit var widgetFactory: WidgetFactory
    lateinit var widgetHolder: WidgetHolder
    lateinit var explicitContainer: Element

    val container: Element by lazy {
        containerNoAfterRender.apply {
            afterRenderCallback()
        }
    }

    val containerNoAfterRender: Element by lazy {
        if (!::explicitContainer.isInitialized)
            explicitContainer = document.createElement("div")
        explicitContainer.apply {
            innerHTML = html
            expand(this)
        }
    }

    fun expand(widget: Widget) {

    }

    private fun expand(element: Element) {
        //find widgets that needs to be expanded
        val toExpand = element.collectWidgetToExpand()
        if (toExpand.isEmpty())
            return
        if (!::widgetFactory.isInitialized)
            throw MissingWidgetFactory(
                "These widget needs to be expanded:" +
                        " [${toExpand.joinToString { it.tagName }}.] in html: [$html] but no ${WidgetFactory::class.simpleName} is available to this widget"
            )
        toExpand.forEach {
            val widget = widgetFactory.new(it.tagName)
            widget.explicitContainer = it
            widget.container //force expand
        }

    }

    private fun Element.collectWidgetToExpand(): List<Element> {
        val dict = children.asList().groupBy { it.tagName.toUpperCase().startsWith("W-") }
        val toExpand = dict[true] ?: emptyList()
        val toScan = dict[false] ?: emptyList()
        val nestedToExpand = toScan.flatMap { it.collectWidgetToExpand() }
        return toExpand + nestedToExpand
    }

    inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>): T {
        val name = property.name
        val element = containerNoAfterRender.querySelector("#$name")
            ?: throw ElementNotFound("Name: [$name] html: [$html]")
        return element as T
    }

    fun close() {
        checkedWidgetHolder.close(this)
    }

    open fun show(widget: Widget) {
        checkedWidgetHolder.show(widget)
    }

    private val checkedWidgetHolder: WidgetHolder
        get() {
            if (!::widgetHolder.isInitialized)
                throw MissingWidgetHolder("html: [$html]")
            return widgetHolder
        }
    var afterRenderCallback: Widget.() -> Unit = { }
}

fun <T : Widget> T.afterRender(lambda: T.() -> Unit) {
    afterRenderCallback = lambda as Widget.() -> Unit
}

class ElementNotFound(msg: String) : Throwable(msg)
class MissingWidgetFactory(msg: String) : Throwable(msg)
class MissingWidgetHolder(msg: String) : Throwable(msg)

class WidgetFactory {
    private val list = mutableMapOf<String, () -> Widget>()
    fun register(name: String, function: () -> Widget) {
        list[name.toUpperCase()] = function
    }

    fun new(name: String): Widget {
        return list[name.toUpperCase()]!!()
    }
}

class WidgetHolder : Widget("") {
    private val stack = mutableListOf<Widget>()

    override fun show(widget: Widget) {
        widget.widgetHolder = this
        stack.add(widget)
        container.children[0]?.remove()
        container.appendChild(widget.container)
    }

    fun closeCurrent() {
        if (stack.size <= 1)
            return
        stack.removeLast()
        show(stack.removeLast())
    }

    fun close(widget: Widget) {
        if (stack.isEmpty() || widget != stack.last())
            return
        closeCurrent()
    }

}