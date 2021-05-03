import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement
import widget.Widget
import widget.afterRender

class RowWidget : Widget(
    //
    """
            |<label><input id="checkbox" type="checkbox"><span id="label">label here!</span></label> 
            |<button id="remove">remove</button>
            |<w-ButtonWidget id="btnWidget"></w-ButtonWidget>
            |""".trimMargin()
) {
    val checkbox: HTMLInputElement by this
    val label: HTMLSpanElement by this
    val remove: HTMLButtonElement by this
    val btnWidget: HTMLElement by this

    init {
        afterRender {
            remove.onclick = {
                container.remove()
            }
            btnWidget.onclick = {
                label.innerHTML += "."
                true
            }

        }
    }
}