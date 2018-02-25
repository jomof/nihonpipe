package com.jomof.nihonpipe.groveler

import java.util.*

private val DEFAULT_SEPARATOR = ','
private val DEFAULT_QUOTE = '"'

fun parseCsvLine(cvsLine: String?, separators: Char = DEFAULT_SEPARATOR, customQuote: Char = DEFAULT_QUOTE): List<String> {
    var separators = separators
    var customQuote = customQuote
    val result = ArrayList<String>()

    //if empty, return!
    if (cvsLine == null && cvsLine!!.isEmpty()) {
        return result
    }

    if (customQuote == ' ') {
        customQuote = DEFAULT_QUOTE
    }

    if (separators == ' ') {
        separators = DEFAULT_SEPARATOR
    }

    var curVal = StringBuffer()
    var inQuotes = false
    var startCollectChar = false
    var doubleQuotesInColumn = false

    val chars = cvsLine.toCharArray()

    for (ch in chars) {

        if (inQuotes) {
            startCollectChar = true
            if (ch == customQuote) {
                inQuotes = false
                doubleQuotesInColumn = false
            } else {

                //Fixed : allow "" in custom quote enclosed
                if (ch == '\"') {
                    if (!doubleQuotesInColumn) {
                        curVal.append(ch)
                        doubleQuotesInColumn = true
                    }
                } else {
                    curVal.append(ch)
                }

            }
        } else {
            if (ch == customQuote) {

                inQuotes = true

                //Fixed : allow "" in empty quote enclosed
                if (chars[0] != '"' && customQuote == '\"') {
                    curVal.append('"')
                }

                //double quotes in column will hit this!
                if (startCollectChar) {
                    curVal.append('"')
                }

            } else if (ch == separators) {

                result.add(curVal.toString())

                curVal = StringBuffer()
                startCollectChar = false

            } else if (ch == '\r') {
                //ignore LF characters
                continue
            } else if (ch == '\n') {
                //the end, break!
                break
            } else {
                curVal.append(ch)
            }
        }
    }
    result.add(curVal.toString())
    return result
}