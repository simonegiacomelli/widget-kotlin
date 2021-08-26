package example

import kotlinx.browser.document
import org.w3c.dom.*
import widget.Widget

class TablePlayWidget : Widget(
    // language=HTML
    """
    <style>
        table, th, td {
            border: 1px solid black;
        }
    </style>
    <h3>Transform a csv into a table</h3>
    <textarea id="idTextarea"></textarea>
    <br><br>
    <label><input type="checkbox" id="idCheckbox">first row is the header</label>
    <br><br>
        
    <table id="idTable">
    <tbody></tbody>
    </table>
    
    <template id="idTemplateTBody"><tr id="idTr"><td id="idTd0"></td></tr></template>
    <template id="idTemplateTHead"><tr id="idTr2"><th>Index</th></tr></template>
""".trimIndent()) {

    private val idTextarea: HTMLTextAreaElement by this
    private val idTable: HTMLTableElement by this
    private val idTemplateTBody: HTMLTemplateElement by this
    private val idTemplateTHead: HTMLTemplateElement by this
    private val idCheckbox: HTMLInputElement by this

    inner class TemplateTBody : Widget(idTemplateTBody) {
        val idTr: HTMLTableRowElement by this
        val idTd0: HTMLTableCellElement by this
    }

    inner class TemplateTHead : Widget(idTemplateTHead) {
        val idTr2: HTMLTableRowElement by this
    }

    override fun afterRender() {
        idTextarea.oninput = { updateTable() }
        idCheckbox.onclick = { updateTable() }
        idTextarea.value = "a,b,c\nd,e,f,g"
        updateTable()
    }

    private fun updateTable() {
        idTable.tBodies[0]?.remove()
        idTable.tHead?.remove()

        val allRows = idTextarea.value.split("\n")
            .filterNot { it.trim().isEmpty() }
            .map { it.split(",") }
        
        val rows = if (idCheckbox.checked) {
            allRows.firstOrNull()?.also { processTHead(it) }
            allRows.drop(1)
        } else allRows

        processTBody(rows)

    }

    private fun processTBody(rows: List<List<String>>) {
        val tbody = document.createElement("tbody")

        rows.forEachIndexed { index, row ->
            val template = TemplateTBody()
            template.idTd0.innerHTML = "$index"
            val tr = template.idTr
            row.forEach { value -> tr.append(document.createElement("td").also { it.innerHTML = value }) }
            tbody.append(tr)
        }
        idTable.append(tbody)
    }

    private fun processTHead(row: List<String>) {

        val thead = document.createElement("thead") as HTMLElement
        val t = TemplateTHead()
        row.forEach { value ->
            t.idTr2.append(document.createElement("th").also { it.innerHTML = value })
        }
        thead.append(t.idTr2)
        idTable.append(thead)
    }

}