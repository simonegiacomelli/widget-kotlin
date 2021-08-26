package example

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLabelElement
import org.w3c.dom.events.MouseEvent
import widget.Widget


class LoginWidget : Widget(
    // language=HTML

    """
            |username: <input type="text" id="user"> <br>
            |password: <input type="text" id="pass"> <br>
            |<br>
            |<button id="login">login</button> 
            |<br>
            |<button id="login2">click here to login without username or password</button>
            |<br>
            |<button id="idTablePlayWidget">TablePlayWidget</button> 
            |<br><br>
            |<label id="idMessage"></label>
            |""".trimMargin()
) {
    private val user: HTMLInputElement by this
    private val pass: HTMLInputElement by this
    private val login: HTMLButtonElement by this
    private val login2: HTMLButtonElement by this
    private val idTablePlayWidget: HTMLButtonElement by this
    private val idMessage: HTMLLabelElement by this

    override fun afterRender() {
        login.onclick = ::loginButtonClick
        login2.onclick = ::showMainWidget
        idTablePlayWidget.onclick = { show(TablePlayWidget()) }
    }

    private fun loginButtonClick(event: MouseEvent) {
        idMessage.innerHTML = ""
        if (user.value.isEmpty() || pass.value.isEmpty())
            idMessage.innerHTML = "Please insert username and password. This is an example and any string will do."
        else
            showMainWidget(event)
    }

    private fun showMainWidget(event: MouseEvent) {
        widgetHolder.show(MainWidget(widgetFactory))
    }
}
