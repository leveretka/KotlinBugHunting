package org.nedz.bughunting.api


data class Issue(val file: String, val ruleName: String, val message: String, val location: Location)

data class Location(val startLine: Int, val startColumn: Int, val endLine: Int, val endColumn: Int)


