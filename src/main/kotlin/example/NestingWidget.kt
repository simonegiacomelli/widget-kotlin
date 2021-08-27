package example

import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import widget.Widget

class NestingWidget : Widget(
    //language=HTML
    """<div id="idDiv">ciao</div>     
        |   <w-example.ButtonWidget></w-example.ButtonWidget>
        |   <br><br>
        |   <button id="idButton">add a TEMP1</button>
    """.trimMargin()
) {
    private val idDiv: HTMLDivElement by this
    private val idButton: HTMLButtonElement by this

    class TEMP1 : Template(
        //language=HTML
        """<template><span id="idSpan">this is a buttonwidget: <w-example.ButtonWidget></w-example.ButtonWidget><br><br></span></template>"""
    ) {

        val idSpan: HTMLSpanElement by this
    }

    override fun afterRender() {
        idButton.onclick = { idDiv.append(TEMP1().also { it.widgetFactory = widgetFactory }.idSpan) }
    }
}