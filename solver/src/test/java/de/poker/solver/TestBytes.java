package de.poker.solver;

import net.openhft.chronicle.bytes.*;
import net.openhft.chronicle.core.annotation.NonNegative;
import net.openhft.chronicle.core.io.ReferenceOwner;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

public class TestBytes implements Bytes {
    byte[] bytes;
    short[] shorts;
    int[] ints;
    private int byteCounter = 0;
    private int shortCounter = 0;
    private int intCounter = 0;


    @Override
    public boolean isDirectMemory() {
        return false;
    }

    @Override
    public BytesStore copy() throws IllegalStateException {
        return null;
    }

    @Override
    public long capacity() {
        return 0;
    }

    @Override
    public long addressForRead(long offset) throws UnsupportedOperationException, BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public long addressForWrite(long offset) throws UnsupportedOperationException, BufferOverflowException, IllegalStateException {
        return 0;
    }

    @Override
    public long addressForWritePosition() throws UnsupportedOperationException, BufferOverflowException, IllegalStateException {
        return 0;
    }

    @Override
    public boolean compareAndSwapInt(long offset, int expected, int value) throws BufferOverflowException, IllegalStateException {
        return false;
    }

    @Override
    public void testAndSetInt(long offset, int expected, int value) throws BufferOverflowException, IllegalStateException {

    }

    @Override
    public boolean compareAndSwapLong(long offset, long expected, long value) throws BufferOverflowException, IllegalStateException {
        return false;
    }

    @Override
    public Object underlyingObject() {
        return null;
    }

    @Override
    public boolean isElastic() {
        return false;
    }

    @Override
    public BytesStore bytesStore() {
        return null;
    }

    @Override
    public void move(long from, long to, long length) throws BufferUnderflowException, IllegalStateException, ArithmeticException {

    }

    @Override
    public Bytes compact() throws IllegalStateException {
        return null;
    }

    @Override
    public Bytes clear() throws IllegalStateException {
        return null;
    }

    @Override
    public int lastDecimalPlaces() {
        return 0;
    }

    @Override
    public void lastDecimalPlaces(int lastDecimalPlaces) {

    }

    @Override
    public boolean lastNumberHadDigits() {
        return false;
    }

    @Override
    public void lastNumberHadDigits(boolean lastNumberHadDigits) {

    }

    @Override
    public BytesPrepender clearAndPad(long length) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public BytesPrepender prewrite(byte[] bytes) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public BytesPrepender prewrite(BytesStore bytes) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public BytesPrepender prewriteByte(byte b) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public BytesPrepender prewriteShort(short i) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public BytesPrepender prewriteInt(int i) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public BytesPrepender prewriteLong(long l) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public byte readByte(long offset) throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public int peekUnsignedByte(long offset) throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public short readShort(long offset) throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public int readInt(long offset) throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public long readLong(long offset) throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public float readFloat(long offset) throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public double readDouble(long offset) throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public byte readVolatileByte(long offset) throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public short readVolatileShort(long offset) throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public int readVolatileInt(long offset) throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public long readVolatileLong(long offset) throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public void nativeRead(long position, long address, long size) throws BufferUnderflowException, IllegalStateException {

    }

    @Override
    public RandomDataOutput writeByte(long offset, byte i8) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public RandomDataOutput writeShort(long offset, short i) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public RandomDataOutput writeInt(long offset, int i) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public RandomDataOutput writeOrderedInt(long offset, int i) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public RandomDataOutput writeLong(long offset, long i) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public RandomDataOutput writeOrderedLong(long offset, long i) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public RandomDataOutput writeFloat(long offset, float d) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public RandomDataOutput writeDouble(long offset, double d) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public RandomDataOutput writeVolatileByte(long offset, byte i8) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public RandomDataOutput writeVolatileShort(long offset, short i16) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public RandomDataOutput writeVolatileInt(long offset, int i32) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public RandomDataOutput writeVolatileLong(long offset, long i64) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public RandomDataOutput write(@NonNegative long writeOffset, byte[] byteArray, @NonNegative int readOffset, @NonNegative int length) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public void write(long writeOffset, ByteBuffer bytes, int readOffset, int length) throws BufferOverflowException, IllegalStateException {

    }

    @Override
    public RandomDataOutput write(long writeOffset, RandomDataInput bytes, long readOffset, long length) throws BufferOverflowException, BufferUnderflowException, IllegalStateException {
        return null;
    }

    @Override
    public void nativeWrite(long address, long position, long size) throws BufferOverflowException, IllegalStateException {

    }

    @Override
    public long write8bit(long position, BytesStore bs) {
        return 0;
    }

    @Override
    public long write8bit(long position, String s, int start, int length) {
        return 0;
    }

    @Override
    public StreamingDataInput readPosition(long position) throws BufferUnderflowException, IllegalStateException {
        return null;
    }

    @Override
    public StreamingDataInput readLimit(long limit) throws BufferUnderflowException {
        return null;
    }

    @Override
    public StreamingDataInput readSkip(long bytesToSkip) throws BufferUnderflowException, IllegalStateException {
        return null;
    }

    @Override
    public void uncheckedReadSkipOne() {

    }

    @Override
    public void uncheckedReadSkipBackOne() {

    }

    @Override
    public byte readByte() throws IllegalStateException {
        byte aByte = bytes[byteCounter];
        byteCounter++;
        return aByte;
    }

    @Override
    public int readUnsignedByte() throws IllegalStateException {
        return 0;
    }

    @Override
    public int uncheckedReadUnsignedByte() {
        return 0;
    }

    @Override
    public short readShort() throws BufferUnderflowException, IllegalStateException {
        short aShort = shorts[shortCounter];
        shortCounter++;
        return aShort;
    }

    @Override
    public int readInt() throws BufferUnderflowException, IllegalStateException {
        int aInt = ints[intCounter];
        intCounter++;
        return aInt;
    }

    @Override
    public long readLong() throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public float readFloat() throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public double readDouble() throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public int readVolatileInt() throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public long readVolatileLong() throws BufferUnderflowException, IllegalStateException {
        return 0;
    }

    @Override
    public int peekUnsignedByte() throws IllegalStateException {
        return 0;
    }

    @Override
    public void lenient(boolean lenient) {

    }

    @Override
    public boolean lenient() {
        return false;
    }

    @Override
    public StreamingDataOutput writePosition(long position) throws BufferOverflowException {
        return null;
    }

    @Override
    public StreamingDataOutput writeLimit(long limit) throws BufferOverflowException {
        return null;
    }

    @Override
    public StreamingDataOutput writeSkip(long bytesToSkip) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public StreamingDataOutput write8bit(String text, int start, int length) {
        return null;
    }

    @Override
    public StreamingDataOutput writeByte(byte i8) throws BufferOverflowException, IllegalStateException {
        if (Objects.isNull(bytes)) {
            bytes = new byte[1];
        } else {
            bytes = Arrays.copyOf(bytes, bytes.length+1);
        }
        bytes[bytes.length-1] = i8;
        return null;
    }

    @Override
    public StreamingDataOutput writeShort(short i16) throws BufferOverflowException, IllegalStateException {
        if (Objects.isNull(shorts)) {
            shorts = new short[1];
        } else {
            shorts = Arrays.copyOf(shorts, shorts.length+1);
        }
        shorts[shorts.length-1] = i16;
        return null;
    }

    @Override
    public StreamingDataOutput writeInt(int i) throws BufferOverflowException, IllegalStateException {
        if (Objects.isNull(ints)) {
            ints = new int[1];
        } else {
            ints = Arrays.copyOf(ints, ints.length+1);
        }
        ints[ints.length-1] = i;
        return null;
    }

    @Override
    public StreamingDataOutput writeIntAdv(int i, int advance) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public StreamingDataOutput writeLong(long i64) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public StreamingDataOutput writeLongAdv(long i64, int advance) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public StreamingDataOutput writeFloat(float f) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public StreamingDataOutput writeDouble(double d) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public StreamingDataOutput writeDoubleAndInt(double d, int i) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public StreamingDataOutput write(byte[] byteArray, @NonNegative int offset, @NonNegative int length) throws BufferOverflowException, IllegalStateException, IllegalArgumentException, ArrayIndexOutOfBoundsException {
        return null;
    }

    @Override
    public StreamingDataOutput writeSome(ByteBuffer buffer) throws BufferOverflowException, IllegalStateException, BufferUnderflowException {
        return null;
    }

    @Override
    public StreamingDataOutput writeOrderedInt(int i) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public StreamingDataOutput writeOrderedLong(long i) throws BufferOverflowException, IllegalStateException {
        return null;
    }

    @Override
    public void reserve(ReferenceOwner id) throws IllegalStateException {

    }

    @Override
    public boolean tryReserve(ReferenceOwner id) throws IllegalStateException, IllegalArgumentException {
        return false;
    }

    @Override
    public boolean reservedBy(ReferenceOwner owner) throws IllegalStateException {
        return false;
    }

    @Override
    public void release(ReferenceOwner id) throws IllegalStateException {

    }

    @Override
    public void releaseLast(ReferenceOwner id) throws IllegalStateException {

    }

    @Override
    public int refCount() {
        return 0;
    }
}
