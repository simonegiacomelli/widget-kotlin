package example

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import widget.Widget
import widget.WidgetHolder

/**
 * Keep a list of widget, keep track of instances and shows them
 */
class GalleryWidget : Widget(
//language=HTML
    """<div id="idGallery"></div><br><br><w-SelectWidget id="idSelectWidget"></w-SelectWidget><br><br><w-WidgetHolder id="idWidgetHolder"></w-WidgetHolder>""") {

    private val idGallery: HTMLDivElement by this
    private val idWidgetHolder: WidgetHolder by this
    private val idSelectWidget: SelectWidget by this

    override fun afterRender() {
        idGallery.innerHTML = ""
        addGallery("TablePlayWidget") { TablePlayWidget() }
        addGallery("ButtonWidget") { ButtonWidget() }
        addGallery("NestingWidget") { NestingWidget() }
        addGallery("ValuesDropDownWidget") { ValuesDropDownWidget() }
        addGallery("GalleryWidget") { GalleryWidget() }
        idSelectWidget.onChange = {
            val itemWidget: ItemWidget? = idSelectWidget.selected()
            if (itemWidget != null) idWidgetHolder.show(itemWidget.widget)
        }
    }

    private fun addGallery(name: String, constr: () -> Widget) {
        val temp1 = Template(
            //language=HTML
            """<template><div id="idDiv"><button id="idBtn"></button><br></div></template>"""
        )
        val idDiv: HTMLDivElement by temp1
        val idBtn: HTMLButtonElement by temp1


        idBtn.innerHTML = name
        idBtn.onclick = {
            val widget = constr().also { it.widgetFactory = widgetFactory }
            idWidgetHolder.show(widget)
            idSelectWidget.addAndSelect(ItemWidget("${idSelectWidget.items.size + 1} - $name", widget))
        }
        idGallery.append(idDiv)
    }

}

class ItemWidget(caption: String, val widget: Widget) : Item(caption)