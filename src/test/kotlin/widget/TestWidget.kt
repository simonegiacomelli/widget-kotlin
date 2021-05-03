package widget

import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
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
    fun withNonExistingDelegate_shouldThrowException() {
        val target = twoDivs()
        assertFailsWith<ElementNotFound> {
            val divDoNotExist: HTMLElement by target
            val s = divDoNotExist.id
        }
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
    fun canUseAfterRenderLambda_AndUseDefinedElements() {
        var callCount = 0

        class Widget1 : Widget("""<button id="button">click me</button>""") {
            val button: HTMLButtonElement by this
        }

        val target = Widget1()
        target.afterRender {
            callCount++
            button.onclick = { }
        }
        assertEquals(0, callCount)
        target.container
        assertEquals(1, callCount)
    }

    @Test
    fun afterRenderShouldBeAbleToAccess_container() {
        val widget = Widget("""<button id="button">click me</button>""")
        widget.afterRender { container }
        widget.container
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


//TODO handle nested widgets
//TODO handle templates
//TODO templates that contains widget, e.g. rackmanager.IpListWidget.html
//TODO handle events, e.g.: afterRender
//TODO WidgetHolder is a ResourceWidget. Is it necessary? just for the elementInstance?
//TODO rethink if the call set(Widget1()).show() can be accepted as show(Widget1())
//TODO is afterRender() good like this? Do we need to extend a class? or is it enough having a lambda?
//TODO handle widget params. e.g.: <w-Widget1><button>hi</button></w-Widget1>


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