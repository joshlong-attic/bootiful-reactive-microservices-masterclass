package com.example.introduction

fun main() {

// variables
	val name = "Josh"
	val name2: Any = "Josh"
	val age: Int = 36
	val names: Array<String> = arrayOf("Joe", "Carol")

// variables with types
	val nameAsString: String = "Jane"
	var mutableString: String = "1"
	mutableString = "2"

	var nullableName: String? = null
	nullableName = "Initialized!"

	// functions
	fun aFunction(name: Int): String {
		return "a string"
	}

	fun anotherFunction(name: String?): Int? = null
	println(anotherFunction("blah"))

	fun yetAnotherFunction(aNum: Double, name: String) = name.length
	println(yetAnotherFunction(10.0, "a Name"))

	val lengthOfString: (String) -> Int = { name -> name.length }
	println(lengthOfString("test"))

	val lengthOfStringWithImplicitParameter: (String) -> Int = { it.length }
	println(lengthOfStringWithImplicitParameter("test"))

	fun takeAnotherFunction(processor: (String) -> Int): List<Int> = arrayOf("aaa", "bb", "ccccccc").map(processor)
	println(takeAnotherFunction({ it.length }))
	println(takeAnotherFunction { it.length })

	// useful functions on variables
	val numberProcessed = 10.let { it * 10 }
	println(numberProcessed)

	"Josh".apply {
		println("[${this}]")
	}

	// expressions
	var sum: Int?
	if (Math.random() > 0.5) {
		sum = 10
	} else {
		sum = 20
	}
	println(sum)

	val sum2 = if (Math.random() > .5) 10 else 20
	println(sum2)

	val xfactor = 2
	val result: String = when (xfactor) {
		1 -> "one"
		2 -> "two"
		3 -> "three"
		else -> "no idea"
	}
	println(result)

	for (i in 0..100) {
		println(i)
	}

	// DSLs
	fun String.announce(name: String) = println("${name} says '${this}'.")
	"foo".announce("josh")


}

// classes
class Cat(initialName: String) {

	private val name: String

	init {
		name = initialName.toUpperCase()
	}
}

class Cat2(private val name: String)

open class Animal // must be open!
class Dog : Animal()

data class GuineaPig(val name: String)