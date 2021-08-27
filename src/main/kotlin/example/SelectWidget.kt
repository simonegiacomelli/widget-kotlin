package example

import extensions.optionsAdd
import extensions.optionsRemoveAll
import org.w3c.dom.HTMLSelectElement
import widget.Widget

class SelectWidget : Widget(/* language=HTML */ """
    <select id="idSelect" style="width:225px;">
    </select>
""".trimIndent()) {
    private val idSelect: HTMLSelectElement by this

    var onChange: () -> Unit = {}

    val items = mutableListOf<Item>()

    override fun afterRender() {
        idSelect.onchange = {
            onChange()
            0
        }
    }

    fun <T : Item> selected(): T? = items.getOrNull(idSelect.selectedIndex) as T?

    fun addAndSelect(item: Item) {
        items.add(item)
        updateUI(item)
    }

    private fun updateUI(selectedItem: Item?) {
        idSelect.optionsRemoveAll()
        items.forEach { item ->
            idSelect.optionsAdd {
                it.text = item.caption
                it.selected = item == selectedItem
            }
        }
    }
}

open class Item(val caption: String)
