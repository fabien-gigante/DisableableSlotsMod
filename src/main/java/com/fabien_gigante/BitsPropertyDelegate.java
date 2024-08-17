package com.fabien_gigante;

import java.util.stream.IntStream;

import net.minecraft.screen.PropertyDelegate;

public class BitsPropertyDelegate implements PropertyDelegate {
    private int size;
    private long bits;

	public BitsPropertyDelegate(int size) { 
        assert size <= 64;
        this.size = size; 
    }
    public void reset() { this.bits = 0; }

	@Override
	public int get(int index) { return (int)(this.bits >> index & 1); }

    @Override
	public void set(int index, int value) {
        assert value == 0 || value == 1;
        this.bits = this.bits & ~(1L<<index) | (((long)value)<<index);
    }

    @Override
	public int size() { return this.size; }

    public IntStream find(int value) {
        assert value == 0 || value == 1;
        return IntStream.range(0, this.size).filter(i -> this.get(i) == value);
    }
    public int count(int value) { 
        assert value == 0 || value == 1;
        return value != 0 ? Long.bitCount(bits) : size - Long.bitCount(bits); 
    }
}