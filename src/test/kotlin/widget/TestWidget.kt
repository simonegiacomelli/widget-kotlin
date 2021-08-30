package widget

import kotlinx.browser.document
import org.w3c.dom.*
import kotlin.test.*

class TestWidget {

    @Test
    fun simpleHtml_shouldBePresentInContainer() {
        val target = Widget("<span>hi joe</span>")
        assert(target.container.innerHTML).contains("hi joe")
    }

    @Test
    fun withNestedWidget_withoutWidgetFactory_shouldComplaintAboutImpossibilityToExpandNestedWidget() {
        val target = Widget("<w-Widget1></w-Widget1>")
        val exception = assertFailsWith<MissingWidgetFactory> { target.container }
        assert(exception.message ?: "").contains(target.html)
    }

    @Test
    fun aWidget_withSpecifiedContainer_shouldUseItAsContainerElement() {
        val target = Widget("<button>click me</button>")
        target.explicitContainer = document.createElement("span")
        assert(target.container.outerHTML).contains("<span>", "click me")
    }

    @Test
    fun shouldProvideDelegateToAccessElements() {
        val target = twoDivs()
        val div3: HTMLElement by target
        assertEquals("baz", div3.innerHTML)
    }

    @Test
    fun withNonExistingElement_shouldThrowException() {
        val target = twoDivs()
        assertFailsWith<ElementNotFound> {
            val divDoNotExist: HTMLElement by target
            val s = divDoNotExist.id
        }
    }

    @Test
    fun shouldProvideDelegateToAccessWidget() {

        class Widget1 : Widget("""<span id="span1">original</span>""") {
            val span1: HTMLSpanElement by this
        }

        val target = Widget("""<w-widget1 id="widget1"></w-widget1>""")
        target.widgetFactory = WidgetFactory().apply { register("w-widget1") { Widget1() } }

        fun testWidget1Type() {
            val widget1: Widget1 by target
            assert(widget1.span1.innerHTML).contains("original")
        }

        fun testWidgetBaseType() {
            val widget1: Widget by target
            assertTrue(Widget1::class.isInstance(widget1))
        }

        fun testHtmlType() {
            val widget1: HTMLElement by target
            assert(widget1.id).contains("widget1")
        }
        testWidget1Type()
        testWidgetBaseType()
        testHtmlType()
    }

    @Test
    fun shouldBeAbleToCloseItself() {
        val widgetHolder = WidgetHolder()

        widgetHolder.show(twoDivs())
        val widget1 = oneDiv()
        val widget2 = oneDiv()
        widgetHolder.show(widget1)
        widgetHolder.show(widget2)

        widget2.close()
        widget1.close()

        assert(widgetHolder.container.innerHTML).contains(twoDivsMarkers)
    }

    @Test
    fun whenNotShown_CloseShouldHaveNoEffect() {
        val widgetHolder = WidgetHolder()

        val widget1 = oneDiv()
        widgetHolder.show(widget1)
        widgetHolder.show(twoDivs())

        widget1.close()

        assert(widgetHolder.container.innerHTML).contains(twoDivsMarkers)
    }

    @Test
    fun aWidgetShownWithWidgetHolder_canShowOtherWidgets() {
        val widgetHolder = WidgetHolder()

        val widget1 = oneDiv()
        val widget2 = twoDivs()

        widgetHolder.show(widget1)
        widget1.show(widget2)

        assert(widgetHolder.container.innerHTML).contains(twoDivsMarkers)
        assert(widgetHolder.container.innerHTML).not.contains(oneDivMarkers)
    }

    @Test
    fun aWidgetWithoutWidgetHolder_thatTriesToShowOrCloseAnotherWidget_shouldTrow() {
        val widget1 = oneDiv()
        val widget2 = twoDivs()

        val exShow = assertFailsWith<MissingWidgetHolder> { widget1.show(widget2) }
        val exClose = assertFailsWith<MissingWidgetHolder> { widget1.close() }

        listOf(exShow, exClose)
            .map { it.message ?: "" }
            .forEach { assert(it).contains(widget1.html) }
    }

    @Test
    fun canUseAfterRenderMethodAndLambda_AndUseDefinedElements() {
        var callCount = 0
        var methodCalled = false
        var lambdaCalled = false

        class Widget1 : Widget("""<button id="button">click me</button>""") {
            val button: HTMLButtonElement by this
            override fun afterRender() {
                methodCalled = true
                callCount++
                button.onclick = { }
            }
        }

        val target = Widget1()
        target.afterRender {
            lambdaCalled = true
            callCount++
            button.onclick = { }
        }
        assertEquals(0, callCount)
        target.container
        assertTrue(lambdaCalled)
        assertTrue(methodCalled)
        assertEquals(2, callCount)
    }

    @Test
    fun afterRenderShouldBeAbleToAccess_container() {
        val widget = Widget("""<button id="button">click me</button>""")
        widget.afterRender { container }
        widget.container
    }

    @Test
    fun namelessParams_PassedByWidget() {
        class Widget1 : Widget("""<button id="btn1">click me</button>""")

        val wf = WidgetFactory().apply { register("w-widget1") { Widget1() } }
        Widget("""<w-Widget1 id="w1"><div>hello</div><div>joe</div></w-Widget1>""").apply {
            widgetFactory = wf
            val w1: Widget1 by this
            val elements = w1.params.elements
            assertEquals(2, elements.size)
            assert(elements[0].innerHTML).contains("hello")
            assert(elements[1].innerHTML).contains("joe")
        }

    }

    @Test
    fun namedParams_PassedByWidget() {
        class Widget1 : Widget("""<button id="btn1">click me</button>""") {
            val div1: HTMLDivElement? by params
            val btn1: HTMLButtonElement by this
        }

        val wf = WidgetFactory().apply { register("w-widget1") { Widget1() } }
        Widget("""<w-Widget1 id="w1"><div id="div1">hello</div></w-Widget1>""").apply {
            widgetFactory = wf
            val w1: Widget1 by this
            assertNotNull(w1.div1)
            assert(w1.div1?.innerHTML ?: "").contains("hello")
            assert(w1.btn1.innerHTML).contains("click me")
        }

    }

    @Test
    fun classCastException_whenElement() {
        val target = Widget("""<button id="btn1"></button>""")
        val btn1: HTMLSpanElement by target
        val exception = assertFailsWith<ClassCastException> { btn1.innerHTML }
        val spanClassName = HTMLSpanElement::class.js.name
        val buttonClassName = HTMLButtonElement::class.js.name
        assert(exception.message ?: "").contains(spanClassName, buttonClassName)
    }

    @Test
    fun classCastException_whenAnotherTypeEntirely() {
        val target = Widget("""<button id="btn1"></button>""")
        val btn1: String by target
        val exception = assertFailsWith<ClassCastException> { btn1.length }
        val buttonClassName = HTMLButtonElement::class.js.name
        assert(exception.message ?: "").contains("String", buttonClassName)
    }

    @Test
    fun template() {
        val target =
            Widget("""<table id="table1"></table><template id="template1"><tr id="tr1"><td id="td1"></td></tr></template>""")

        val table1: HTMLTableElement by target
        val template1: HTMLTemplateElement by target

        class Template1 : Widget(template1) {
            val tr1: HTMLTableRowElement by this
            val td1: HTMLTableCellElement by this
        }

        table1.createTBody()
        val t1 = Template1().also { it.td1.innerHTML = "foo" }
        val t2 = Template1().also { it.td1.innerHTML = "bar" }
        assert(target.container.innerHTML).not.contains("foo", "bar")
        println(target.container.innerHTML)
        table1.tBodies[0]!!.append(t1.tr1)
        table1.tBodies[0]!!.append(t2.tr1)
        println(target.container.innerHTML)
        assert(target.container.innerHTML).contains("foo", "bar")
    }

    @Test
    fun useWidgetWithGenerics() {

        class Gen1<T> : Widget("""<div id="idDiv"></div>""") {
            val idDiv: HTMLDivElement by this
            fun add(obj: T) {
                idDiv.innerHTML += obj.toString()
            }
        }

        class HelperItem(val str: String) {
            override fun toString() = "-= $str =-"
        }

        val target = Widget("""<gen-w id="id1"></gen-w>""")
        val id1 by target.create { Gen1<HelperItem>() }

        id1.add(HelperItem("foo"))
        id1.add(HelperItem("bar"))
        assert(target.container.innerHTML).contains("-= foo =-", "-= bar =-")
    }
}

class TestWidgetFactory {

    @Test
    fun aWidget_withNestedWidget_withWidgetFactory_shouldExpandCorrectly() {
        val widgetFactory = WidgetFactory()
        widgetFactory.register("w-widget1") { Widget("<button>click me</button>") }
        val target = Widget("<w-Widget1></w-Widget1>")
        target.widgetFactory = widgetFactory
        assert(target.container.innerHTML).contains("click me", "w-widget1")
    }

    @Test
    fun aWidget_withNestedWidgetAtLevel2_withWidgetFactory_shouldExpandCorrectly() {
        val widgetFactory = WidgetFactory()
        widgetFactory.register("w-widget1") { Widget("<button>click me</button>") }
        val target = Widget("<span><w-Widget1></w-Widget1></span>")
        target.widgetFactory = widgetFactory
        assert(target.container.innerHTML).contains("click me", "w-widget1")
    }

}

class TestWidgetHolder {
    @Test
    fun shouldEasilyShowAWidget() {
        val target = WidgetHolder()
        target.show(twoDivs())
        assert(target.container.innerHTML).contains(twoDivsMarkers)
    }

    @Test
    fun showShouldRemovePreviousWidget() {
        val target = WidgetHolder()

        target.show(twoDivs())
        target.show(oneDiv())

        assert(target.container.innerHTML).not.contains(twoDivsMarkers)
    }

    @Test
    fun closeShouldRestorePreviousWidget() {
        val target = WidgetHolder()

        assert(target.container.innerHTML).not.contains(twoDivsMarkers)

        target.show(twoDivs())
        target.show(oneDiv())
        target.show(oneDiv())
        target.closeCurrent()
        target.closeCurrent()

        assert(target.container.innerHTML).contains(twoDivsMarkers)
    }

    @Test
    fun closeShouldNeverRemoveLastWidget() {
        val target = WidgetHolder()

        target.show(twoDivs())
        target.closeCurrent()

        assert(target.container.innerHTML).contains(twoDivsMarkers)
    }

//TODO HTMLSpanElement by this when actually it is a HTMLButtonElement
//TODO handle nested widgets
//TODO handle templates
//TODO templates that contains widget, e.g. rackmanager.IpListWidget.html
//TODO handle events, e.g.: afterRender
//TODO WidgetHolder is a ResourceWidget. Is it necessary? just for the elementInstance?
//TODO is afterRender() good like this? Do we need to extend a class? or is it enough having a lambda?

}

class TestAssert {

    @Test
    fun AssertContainsFails() {
        assertFails { assert("one two").contains(listOf("three", "four")) }
    }

    @Test
    fun AssertNotContains() {
        assert("one two").not.contains(listOf("three", "four"))
        assert("one two").not.contains("three", "four")
    }

    @Test
    fun AssertNotContainsFails() {
        assertFails { assert("one two").not.contains(listOf("one", "two")) }
    }

    @Test
    fun AssertContains() {
        assert("one two").contains(listOf("one", "two"))
    }

}

private fun oneDiv() = Widget("""<div id="div1">foo</div>""")
private fun twoDivs() = Widget("""<div id="div2">bar</div><div id="div3">baz</div>""")

private val twoDivsMarkers = listOf("div2", "bar", "div3", "baz")
private val oneDivMarkers = listOf("div1", "foo")


class Assert(private val str: String, val notEnabled: Boolean = false) {
    val not by lazy { Assert(str, true) }
    fun contains(list: List<String>) = contains(*list.toTypedArray())
    fun contains(vararg list: String) {
        val bad = list.filter { str.contains(it).xor(!notEnabled) }
        if (bad.isEmpty()) return
        val conj = if (!notEnabled) " not" else ""
        fail("failed because string [$str] do$conj contains $bad")
    }
}

private fun assert(str: String) = Assert(str)