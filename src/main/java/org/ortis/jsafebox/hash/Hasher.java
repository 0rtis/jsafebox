
package org.ortis.jsafebox.hash;

public interface Hasher
{

	public byte [] hash(final byte [] data);

	public int getHashLength();

	default byte [] getEmptyHash()
	{
		return new byte[getHashLength()];
	}

}
