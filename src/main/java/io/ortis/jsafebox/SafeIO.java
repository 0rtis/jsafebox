
package io.ortis.jsafebox;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import javax.crypto.Cipher;

import io.ortis.jsafebox.task.TaskProbe;

/**
 * Utility class for IO operation on {@link Safe}
 * 
 * @author Ortis <br>
 *         2018 Jun 22 5:47:46 PM <br>
 */
public abstract class SafeIO
{
	public static long encrypt(final InputStream data, final Cipher cipher, final RandomAccessFile destination, final byte [] buffer, final byte [] bufferDecrypted, final TaskProbe probe)
			throws Exception
	{
		long total = 0;
		int read;
		while ((read = data.read(bufferDecrypted)) > -1)
		{
			read = cipher.update(bufferDecrypted, 0, read, buffer);
			if (read == 0)
				// data length is less than cipher block size
				System.arraycopy(bufferDecrypted, 0, buffer, 0, bufferDecrypted.length);

			total += read;
			destination.write(buffer, 0, read);

			probe.checkCancel();
		}

		read = cipher.doFinal(buffer, 0);
		destination.write(buffer, 0, read);
		total += read;

		return total;
	}

	public static void decrypt(final RandomAccessFile source, final long length, final Cipher cipher, final OutputStream destination, final byte [] bufferEncrypted, final byte [] bufferDecrypted)
			throws Exception
	{
		long remaining = length;
		int read;
		while (remaining > 0)
		{
			if (remaining < bufferEncrypted.length)
				read = source.read(bufferEncrypted, 0, (int) remaining);
			else
				read = source.read(bufferEncrypted, 0, bufferEncrypted.length);

			remaining -= read;

			read = cipher.update(bufferEncrypted, 0, read, bufferDecrypted);
			destination.write(bufferDecrypted, 0, read);
		}

		read = cipher.doFinal(bufferDecrypted, 0);
		destination.write(bufferDecrypted, 0, read);
	}

	public static long copy(final InputStream data, final RandomAccessFile destination, final byte [] buffer, final TaskProbe probe) throws Exception
	{
		long total = 0;
		int read;
		while ((read = data.read(buffer)) > -1)
		{
			destination.write(buffer, 0, read);
			total += read;

			probe.checkCancel();
		}

		return total;
	}

	public static void copy(final RandomAccessFile source, final long length, final OutputStream destination, final byte [] buffer, final TaskProbe probe) throws Exception
	{
		long remaining = length;
		int read;
		while (remaining > 0)
		{
			if (remaining < buffer.length)
				read = source.read(buffer, 0, (int) remaining);
			else
				read = source.read(buffer, 0, buffer.length);

			destination.write(buffer, 0, read);

			remaining -= read;

			probe.checkCancel();
		}
	}

	public static void copy(final RandomAccessFile source, final long length, final RandomAccessFile destination, final byte [] buffer, final TaskProbe probe) throws Exception
	{
		long remaining = length;
		int read;
		while (remaining > 0)
		{
			if (remaining < buffer.length)
				read = source.read(buffer, 0, (int) remaining);
			else
				read = source.read(buffer, 0, buffer.length);

			destination.write(buffer, 0, read);

			remaining -= read;

			probe.checkCancel();
		}
	}
}
