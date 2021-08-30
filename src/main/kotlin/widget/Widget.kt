package widget

import kotlinx.browser.document
import org.w3c.dom.*
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class Widget(val html: String) {
    var htmlTemplate = false

    constructor(template: HTMLTemplateElement) : this("") {
        htmlTemplate = true
        val array: Array<Node> = document.importNode(template.content, true).childNodes.asList().toTypedArray()
        explicitContainer = document.createElement("div")
        explicitContainer.append(*array)
    }

    fun log(msg: String) = console.log("$msg\n")
    var namedDescendant: Map<String, Widget> = emptyMap()
    lateinit var widgetFactory: WidgetFactory
    lateinit var widgetHolder: WidgetHolder
    lateinit var explicitContainer: Element

    private val expandOnce = OnlyOnce { expandContainer(); }

    val container: Element get() = computeContainer()

    private fun computeContainer(): Element {
        expandOnce.invoke(); return explicitContainer
    }

    private fun expandContainer() {
        if (::explicitContainer.isInitialized)
            params.elements.addAll(explicitContainer.children.asList())
        else
            explicitContainer = document.createElement("div")
        val container = explicitContainer
        if (!htmlTemplate)
            container.innerHTML = html
        expandContainedWidgetIfAny(container)
        afterRender()
        afterRenderCallback()
    }

    private fun expandContainedWidgetIfAny(element: Element) {
        val toExpand = element.collectWidgetToExpand()
        if (toExpand.isEmpty())
            return
        if (!::widgetFactory.isInitialized)
            throw MissingWidgetFactory(
                "These widget needs to be expanded:" +
                        " [${toExpand.joinToString { it.tagName }}] but no ${WidgetFactory::class.simpleName} is available to this widget having html: [$html] "
            )
        namedDescendant = toExpand.map {
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

    fun getElement(property: KProperty<*>): Element = container.querySelector("#${property.name}")
        ?: throw ElementNotFound("Name: [${property.name}] html: [$html]")

    inline operator fun <reified T> getValue(thisRef: Any?, property: KProperty<*>): T {
        container // force expand to collect descendants
        val widget = this.namedDescendant[property.name]

        if (widget != null && T::class.isInstance(widget)) {
            return widget as T
        }
        val element = getElement(property)
        if (!T::class.isInstance(element))
            throw ClassCastException(
                "Element instance is of type ${element::class.js.name}" +
                        " but delegate is of type ${(T::class as KClass<out Element>).js.name}. html: [$html]"
            )

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

    inline fun <reified T : Widget> create(crossinline function: () -> T) =
        PropertyDelegateProvider { _: Any?, property ->
            val instance = lazy { function().also { it.explicitContainer = getElement(property) } }
            ReadOnlyProperty<Any?, T> { _, _ -> instance.value }
        }


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
        list[name.uppercase()] = function
    }

    fun new(name: String): Widget {
        return list[name.uppercase()]!!()
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