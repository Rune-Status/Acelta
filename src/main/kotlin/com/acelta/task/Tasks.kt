package com.acelta.task

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import java.util.concurrent.Executors.newSingleThreadScheduledExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS

object Tasks {

	const val CYCLE_MS = 600L

	private val tasks = ObjectArrayList<Task>()
	private val executor = newSingleThreadScheduledExecutor()

	init {
		executor.scheduleAtFixedRate({
			val start = System.nanoTime()
			try {
				tick()
			} catch (t: Throwable) {
				t.printStackTrace()
			}
			val elapsed = System.nanoTime() - start
			println("Elapsed tick: ${(elapsed / 1000000.0)}ms")
		}, CYCLE_MS, CYCLE_MS, MILLISECONDS)
	}

	fun execute(body: () -> Any) = executor.submit(body)

	fun tick() {
		val it = tasks.iterator()
		while (it.hasNext()) {
			val next = it.next()
			if (next != null && next.finish()) it.remove()
		}
	}

	operator fun plusAssign(task: Task) {
		tasks.add(task)
	}

	inline fun delayed(ticks: Int = 1, crossinline body: () -> Any) = object : TickTask(ticks) {
		override fun run() {
			body()
			stop()
		}
	}

	inline fun repeating(ticks: Int = 1, crossinline body: () -> Boolean) = object : TickTask(ticks) {
		override fun run() {
			if (body()) stop()
		}
	}

	inline fun continuous(ticks: Int = 1, crossinline body: () -> Any) = repeating(ticks) { body(); false }

}

operator fun Task.unaryPlus() {
	Tasks += this
}