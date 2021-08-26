package example

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLabelElement
import org.w3c.dom.events.MouseEvent
import widget.Widget

private var counter = 0

class LoginWidget : Widget(
    """
            |username: <input type="text" id="user"> <br>
            |password: <input type="text" id="pass"> <br>
            |<br>
            |<button id="login">login</button> 
            |<br><br>
            |<label id="idMessage"></label>
            |""".trimMargin()
) {
    private val user: HTMLInputElement by this
    private val pass: HTMLInputElement by this
    private val login: HTMLButtonElement by this
    private val idMessage: HTMLLabelElement by this

    override fun afterRender() {
        login.onclick = ::loginButtonClick
    }

    private fun loginButtonClick(event: MouseEvent) {
        idMessage.innerHTML = ""
        if (user.value.isEmpty() || pass.value.isEmpty())
            idMessage.innerHTML = "Please insert username and password. This is an example and any string will do."
        else
            widgetHolder.show(MainWidget(widgetFactory) { counter++; counter })
    }
}
