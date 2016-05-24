package com.acelta.packet

import com.acelta.packet.Packeteer.AccessMode
import com.acelta.util.StringCache
import com.acelta.util.nums.int
import com.acelta.util.nums.usin
import io.netty.buffer.ByteBuf
import io.netty.util.concurrent.FastThreadLocal

class ByteBufPacketeer(data: ByteBuf? = null) : Packeteer {

	companion object {
		internal val chars = object : FastThreadLocal<CharArray>() {
			override fun initialValue() = CharArray(256)
		}
	}

	lateinit var data: ByteBuf

	init {
		if (data != null) this.data = data
	}

	override var readIndex: Int
		get() = data.readerIndex()
		set(value) {
			data.setIndex(value, writeIndex)
		}

	override fun get(index: Int) = data.getByte(index)

	override fun skip(bytes: Int) {
		ensureAccessMode(AccessMode.BYTE)
		data.skipBytes(bytes)
	}

	override val readable: Int
		get() = data.readableBytes()

	override val byte: Byte
		get() {
			ensureAccessMode(AccessMode.BYTE)
			return data.readByte()
		}
	override val short: Short
		get() {
			ensureAccessMode(AccessMode.BYTE)
			return data.readShort()
		}
	override val int: Int
		get() {
			ensureAccessMode(AccessMode.BYTE)
			return data.readInt()
		}
	override val long: Long
		get() {
			ensureAccessMode(AccessMode.BYTE)
			return data.readLong()
		}

	override val string: String
		get() {
			ensureAccessMode(AccessMode.BYTE)

			val tChars = chars.get()

			var index = 0
			while (readable > 0) {
				val char = byte.usin.toChar()
				if ('\n' == char) break
				tChars[index++] = char
			}
			return StringCache[tChars, index]
		}

	override var writeIndex: Int
		get() = data.writerIndex()
		set(value) {
			data.setIndex(readIndex, value)
		}

	override fun clear() = apply { data.clear() }

	override fun ensureWritable(bytes: Int) = apply { data.ensureWritable(bytes) }

	override fun set(index: Int, value: Int) {
		data.setByte(index, value)
	}

	override fun plus(values: ByteArray) = apply {
		ensureAccessMode(AccessMode.BYTE)
		data.writeBytes(values)
	}

	override fun plus(value: Byte) = apply {
		ensureAccessMode(AccessMode.BYTE)
		data.writeByte(value.int)
	}

	override fun plus(value: Short) = apply {
		ensureAccessMode(AccessMode.BYTE)
		data.writeShort(value.int)
	}

	override fun plus(value: Int) = apply {
		ensureAccessMode(AccessMode.BYTE)
		data.writeInt(value)
	}

	override fun plus(value: Long) = apply {
		ensureAccessMode(AccessMode.BYTE)
		data.writeLong(value)
	}

	override var accessMode = AccessMode.BYTE

	override var bitIndex = 0

}