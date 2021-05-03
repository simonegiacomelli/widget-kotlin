import org.w3c.dom.HTMLButtonElement
import widget.Widget
import widget.afterRender

class ButtonWidget : Widget("""<button id="button">click me</button>""") {
    val button: HTMLButtonElement by this

    init {
        afterRender {
            button.innerHTML += " (done)"
        }
    }
}