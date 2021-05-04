package widget

import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.asList
import org.w3c.dom.get
import kotlin.reflect.KProperty

open class Widget(val html: String) {

    fun log(msg: String) = console.log("$msg\n")
    var namedDescendant: Map<String, Widget> = emptyMap()
    lateinit var widgetFactory: WidgetFactory
    lateinit var widgetHolder: WidgetHolder
    lateinit var explicitContainer: Element

    private val expandOnce = OnlyOnce { expandContainer(); }

    val container: Element by lazy { expandOnce.invoke(); explicitContainer }

    private fun expandContainer() {
        if (::explicitContainer.isInitialized)
            params.elements.addAll(explicitContainer.children.asList())
        else
            explicitContainer = document.createElement("div")

        val element = explicitContainer
        element.innerHTML = html
        val toExpand = element.collectWidgetToExpand()
        if (toExpand.isNotEmpty())
            expandContainedWidgets(toExpand)
        afterRender()
        afterRenderCallback()
    }

    private fun expandContainedWidgets(toExpand: List<Element>) {
        if (!::widgetFactory.isInitialized)
            throw MissingWidgetFactory(
                "These widget needs to be expanded:" +
                        " [${toExpand.joinToString { it.tagName }}] but no ${WidgetFactory::class.simpleName} is available to this widget having html: [$html] "
            )
        this.namedDescendant = toExpand.map {
            val widget = widgetFactory.new(it.tagName)
            widget.explicitContainer = it
            widget.container //force expand
            widget
        }.filter { it.container.id.isNotBlank() }.associateBy({ it.container.id }, { it })
    }

    private fun Element.collectWidgetToExpand(): List<Element> {
        val dict = children.asList().groupBy { it.tagName.toUpperCase().startsWith("W-") }
        val toExpand = dict[true] ?: emptyList()
        val toScan = dict[false] ?: emptyList()
        val nestedToExpand = toScan.flatMap { it.collectWidgetToExpand() }
        return toExpand + nestedToExpand
    }

    inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>): T {
        container // force expand to collect descendants
        val name = property.name
        val widget = this.namedDescendant[name]

        if (widget != null && T::class.isInstance(widget)) {
            return widget as T
        }
        val element = container.querySelector("#$name")
            ?: throw ElementNotFound("Name: [$name] html: [$html]")
        return element as T
    }

    val params = Params()

    class Params {
        val elements = mutableListOf<Element>()
        inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>): T? {
            return elements.find { it.id == property.name } as T?
        }
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
    open fun afterRender() {}
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
    val stack = mutableListOf<Widget>()

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

class OnlyOnce(val function: () -> Unit) {
    var called = false
    fun invoke() {
        if (called) return
        called = true
        function()
    }
}