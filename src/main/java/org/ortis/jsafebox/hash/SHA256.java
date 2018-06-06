
package org.ortis.jsafebox.hash;

import java.security.MessageDigest;

public class SHA256 implements Hasher
{

	private final MessageDigest md;

	public SHA256()
	{
		try
		{
			this.md = MessageDigest.getInstance("SHA-256");
		} catch (final Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte [] hash(byte [] data)
	{
		return md.digest(data);
	}

	

	@Override
	public int getHashLength()
	{
		return 32;
	}

}
