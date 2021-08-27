package example

import kotlinx.browser.window
import org.w3c.dom.*
import org.w3c.dom.events.Event
import extensions.optionsAdd
import extensions.optionsRemoveAll
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import widget.Widget

class ValuesDropDownWidget : Widget( //language=HTML
    """<div id="guided">
    <select id="select1" style="width:225px;">
        <option value=""></option>
    </select> <a id="lnkAdd" href="#">Add</a> / <a id="lnkEdit" href="#">Edit</a>
</div>

<div id="manual" style="display: none;">
    <input type="text" id="input1" style="width:225px; ">
    <a id="lnkSave" href="#">Save</a> / <a id="lnkCancel" href="#">Cancel</a>
</div>""") {
    val docu = this

    val select1: HTMLSelectElement by docu
    val input1: HTMLInputElement by docu

    val lnkAdd: HTMLElement by docu
    val lnkEdit: HTMLElement by docu
    val lnkSave: HTMLElement by docu
    val lnkCancel: HTMLElement by docu

    val manual: HTMLElement by docu
    val guided: HTMLElement by docu

    val section: String get() = container.getAttribute("w-section").orEmpty()
    val name: String get() = container.getAttribute("w-name").orEmpty()

    val valueList = mutableSetOf<String>()
    val loadOnce = lazy { }
    var value: String
        get() = input1.value
        set(value) {
            input1.value = value
            ensureManualInGuided()
        }

    private fun ensureManualInGuided() {

    }

    enum class State { GUIDED, EDIT, ADD }

    var state: State = State.GUIDED

    var oldValue = ""

    override fun afterRender() {

        select1.onchange = {
            val opt = select1.selectedOptions.asList().first() as HTMLOptionElement
            input1.value = opt.text
            0
        }
        lnkAdd.onclickExt {
            oldValue = input1.value
            input1.value = ""
            changeToInput()
            state = State.ADD
        }
        lnkEdit.onclickExt {
            oldValue = input1.value
            changeToInput()
            state = State.EDIT
        }
        lnkSave.onclickAsync {
            if (state == State.ADD) {
                save()
            }
            if (state == State.EDIT) {
                valueList.remove(oldValue)
                save()
            }
            changeToSelect()
            state = State.GUIDED
        }
        lnkCancel.onclickExt {
            input1.value = oldValue
            changeToSelect()
            state = State.GUIDED
        }
        GlobalScope.async { loadValues() }
    }

//        afterShow {
//            async {
//                select1.disabled = true
//                displayValueListInSelect()
//                loadValues()
//            }
//        }
//


    private suspend fun save() {
        if (!loadOnce.isInitialized()) {
            window.alert("Error: No data loaded. Cannot continue")
            return
        }
        input1.value = input1.value.trim()
        if (input1.value.isNotEmpty())
            valueList.add(input1.value)
        displayValueListInSelect()
//        notImplemented()
//        val tab = Api.apiValuesList.new {
//            it.section = section
//            it.name = name
//        }.call { }.await().table
//
//        if (tab.Rows.size == 0) {
//            tab.Rows.add()
//        }
//        val row = tab.Rows.first()
//        row.section = section
//        row.name = name
//        row.content = valueList.joinToString("\n")
//        Api.apiValuesIUD.new {
//            it.table = tab
//        }.call { }.await()
    }

    private fun notImplemented() {
        window.alert("Error: No data loaded. Cannot continue")
        TODO()
    }

    private fun displayValueListInSelect() {
        select1.optionsRemoveAll()
        select1.optionsAdd {
            it.text = ""
        }
        if (input1.value.isNotEmpty() && !valueList.contains(input1.value)) {
            select1.optionsAdd {
                it.text = input1.value
                it.selected = true
                it.disabled = true
            }
        }
        valueList.sorted().forEach { value ->
            select1.optionsAdd {
                it.text = value
                if (value == input1.value)
                    it.selected = true
            }
        }
    }

    private suspend fun loadValues() {
//        notImplemented()
//        val row = Api.apiValuesList.new {
//            it.section = section
//            it.name = name
//        }.call {}.await().table.Rows.firstOrNull()
//        valueList.clear()
//        if (row != null)
//            valueList.addAll(row.content.orEmpty().split("\n"))
        loadOnce.value
        select1.disabled = false
        displayValueListInSelect()
    }

    fun changeToSelect() {
        guided.style.display = ""
        manual.style.display = "none"
    }

    fun changeToInput() {
        guided.style.display = "none"
        manual.style.display = ""
        input1.focus()
    }
}

private fun HTMLElement.onclickExt(value: (Event) -> Unit) {
    this.onclick = {
        it.preventDefault()
        it.stopPropagation()
        value(it)
        0
    }
}

private fun HTMLElement.onclickAsync(value: suspend (Event) -> Unit) {
    this.onclick = {
        it.preventDefault()
        it.stopPropagation()

        GlobalScope.async {
            async {
                value(it)
            }
        }
    }
}
