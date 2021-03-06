package com.acelta.packet

import com.acelta.packet.Packeteer.AccessMode
import com.acelta.util.StringCache
import com.acelta.util.nums.int
import com.acelta.util.nums.usin
import io.netty.buffer.ByteBuf
import io.netty.buffer.PooledByteBufAllocator
import io.netty.util.concurrent.FastThreadLocal

class ByteBufPacketeer(buf: ByteBuf? = null) : Packeteer {

	companion object {

		private val chars = CharArray(256)

		/* visible for inline */ var reusablesCount = 0
		/* visible for inline */ val reusables = Array(2048) {
			ByteBufPacketeer(PooledByteBufAllocator.DEFAULT.directBuffer())
		}

		inline fun reusable(crossinline body: ByteBufPacketeer.() -> Any) {
			if (++reusablesCount >= reusables.size) reusablesCount = 0
			with(reusables[reusablesCount]) {
				clear()
				body()
			}
		}

		inline fun reusable(out: Packeteer, crossinline body: ByteBufPacketeer.() -> Any) {
			reusable {
				body()
				out + this
			}
		}

	}

	lateinit var buf: ByteBuf

	init {
		if (buf != null) this.buf = buf
	}

	override var readIndex: Int
		get() = buf.readerIndex()
		set(value) {
			buf.setIndex(value, writeIndex)
		}

	override fun get(index: Int) = buf.getByte(index)

	override fun skip(bytes: Int) {
		ensureAccessMode(AccessMode.BYTE)
		buf.skipBytes(bytes)
	}

	override val readable: Int
		get() = buf.readableBytes()

	override val byte: Byte
		get() {
			ensureAccessMode(AccessMode.BYTE)
			return buf.readByte()
		}
	override val short: Short
		get() {
			ensureAccessMode(AccessMode.BYTE)
			return buf.readShort()
		}
	override val int: Int
		get() {
			ensureAccessMode(AccessMode.BYTE)
			return buf.readInt()
		}
	override val long: Long
		get() {
			ensureAccessMode(AccessMode.BYTE)
			return buf.readLong()
		}

	override val string: String
		get() {
			ensureAccessMode(AccessMode.BYTE)

			var index = 0
			while (readable > 0) {
				val char = byte.usin.toChar()
				if ('\n' == char) break
				chars[index++] = char
			}
			return StringCache[chars, index]
		}

	override var writeIndex: Int
		get() = buf.writerIndex()
		set(value) {
			buf.setIndex(readIndex, value)
		}

	override fun clear() = apply { buf.clear() }

	override fun ensureWritable(bytes: Int) = apply { buf.ensureWritable(bytes) }

	override fun set(index: Int, value: Int) {
		buf.setByte(index, value)
	}

	override fun plus(values: ByteArray) = apply {
		ensureAccessMode(AccessMode.BYTE)
		buf.writeBytes(values)
	}

	override fun plus(value: Packeteer) = apply {
		ensureAccessMode(AccessMode.BYTE)
		if (value is ByteBufPacketeer) buf.writeBytes(value.buf)
		else throw IllegalArgumentException("`ByteBufPacketeer` can only write other `ByteBufPacketeer`s")
	}

	override fun plus(value: Byte) = apply {
		ensureAccessMode(AccessMode.BYTE)
		buf.writeByte(value.int)
	}

	override fun plus(value: Short) = apply {
		ensureAccessMode(AccessMode.BYTE)
		buf.writeShort(value.int)
	}

	override fun plus(value: Int) = apply {
		ensureAccessMode(AccessMode.BYTE)
		buf.writeInt(value)
	}

	override fun plus(value: Long) = apply {
		ensureAccessMode(AccessMode.BYTE)
		buf.writeLong(value)
	}

	override var accessMode = AccessMode.BYTE

	override var bitIndex = 0

}