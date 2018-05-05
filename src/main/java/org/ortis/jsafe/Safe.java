/*******************************************************************************
 * Copyright 2018 Ortis (cao.ortis.org@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 ******************************************************************************/

package org.ortis.jsafe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.security.spec.AlgorithmParameterSpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.ortis.jsafe.task.TaskProbe;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Virtual vault where files are stored
 * 
 * @author Ortis <br>
 *         2018 Apr 26 7:29:29 PM <br>
 */
public class Safe implements Closeable
{

	private final static DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	public final static String UTF8 = "UTF-8";
	public final static Gson GSON = new Gson();
	private final static Type MAP_STRING_STRING_TYPE = new TypeToken<Map<String, String>>()
	{
	}.getType();

	public final static Type BYTE_ARRAY_TYPE = new TypeToken<byte []>()
	{
	}.getType();

	private final static TaskProbe EMPTY_PROBE = new TaskProbe()
	{

		@Override
		public boolean isCancelRequested()
		{
			return false;
		}

		@Override
		public void fireException(final Exception exception)
		{
		}

		@Override
		public void fireTerminated()
		{
		}

		@Override
		public void fireProgress(final double progress)
		{
		}

		@Override
		public void fireMessage(final String message)
		{
		}

		@Override
		public void fireCanceled()
		{
		}
	};

	public final static String ENCRYPTION_LABEL = "encryption";
	public final static String ENCRYPTION_IV_LABEL = "iv";
	public final static String KEY_ALGO_LABEL = "algo";
	public final static String PROTOCOL_SPEC_LABEL = "protocol description";

	public final static String PROTOCOL_SPEC = "JSafe is using a very simple protocol so encrypted files can be easily read by another program, as long as you have the encryption password.Each datagram is preceded by its length stored as a 64 bits (8 bytes) integer (`long` in Java): length 0|datagram 0|length 1|datagram 1|length 3|...|datagram N. The first datagram `datagram 0` is the *header* and is **the only datagram not encrypted**. The *header* contains text entries specified by the user and various additional entries incuding a protocol explanation, the type of encoding and the IV of the encryption. The *header*'s data is stored in JSON format and can be seen by opening the safe file with a basic text editor. The second datagram `datagram 1` is the *properties*. It contains encrypted text entries specified by the user. The following datagrams (from 2 to N) are the encrypted files. They work by pair: `datagram i ` contains the metadata of the file as an encrypted JSON text and `datagram i+1` contains the bytes of the encrypted file.";

	private final File originalFile;

	private final Cipher cipher;
	private final SecretKeySpec keySpec;
	private final AlgorithmParameterSpec algoSpec;

	private final RandomAccessFile original;

	private final File tempFile;
	private final RandomAccessFile temp;

	private final Map<String, String> publicHeader;
	private final Map<String, String> privateProperties;
	private final Map<String, Block> roBlocks;
	private final Map<String, Block> blocks;
	private final Map<String, Block> tempBlocks;
	private final Map<String, Block> deletedBlocks;

	private final int bufferSize;

	private final Folder root;

	/**
	 * Create an instance of {@link Safe}
	 * 
	 * @param file:
	 *            the safe file
	 * @param cipher:
	 *            cipher to decrypt the data
	 * @param keySpec:
	 *            key specification
	 * @param algoSpec:
	 *            encryption specification
	 * @param bufferSize:
	 *            size of the <code>byte</code> buffer to be used in IO operation
	 * @throws Exception
	 */
	public Safe(final File file, final Cipher cipher, final SecretKeySpec keySpec, final AlgorithmParameterSpec algoSpec, final int bufferSize) throws Exception
	{

		this.originalFile = file;
		this.cipher = cipher;

		this.keySpec = keySpec;
		this.algoSpec = algoSpec;
		this.bufferSize = bufferSize;

		this.cipher.init(Cipher.DECRYPT_MODE, this.keySpec, this.algoSpec);
		this.original = new RandomAccessFile(file, "rw");
		this.tempFile = Files.createTempFile(null, null).toFile();
		this.temp = new RandomAccessFile(this.tempFile, "rw");

		final HashMap<String, String> publicProps = new LinkedHashMap<>();
		this.publicHeader = Collections.unmodifiableMap(publicProps);
		final HashMap<String, String> props = new LinkedHashMap<>();
		this.privateProperties = Collections.unmodifiableMap(props);
		this.blocks = new LinkedHashMap<>();
		this.roBlocks = Collections.unmodifiableMap(blocks);
		this.tempBlocks = new LinkedHashMap<>();
		this.deletedBlocks = new LinkedHashMap<>();
		this.root = new Folder(null, Folder.ROOT_NAME);

		final byte [] buffer = new byte[bufferSize];
		final byte [] outBuffer = new byte[bufferSize];

		long length;
		int read;

		final ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);

		final long headerLength = this.original.readLong();

		length = headerLength;

		while (length > 0)

		{

			if (length < buffer.length)
				read = this.original.read(buffer, 0, (int) length);
			else
				read = this.original.read(buffer);

			baos.write(buffer, 0, read);
			length -= read;

		}

		String json = new String(baos.toByteArray(), UTF8);

		publicProps.putAll(GSON.fromJson(json, MAP_STRING_STRING_TYPE));

		// read private privateProperties
		final long propLength = this.original.readLong();
		length = propLength;

		baos.reset();

		while (length > 0)
		{

			if (length < buffer.length)
				read = this.original.read(buffer, 0, (int) length);
			else
				read = this.original.read(buffer);

			final int decrypted = cipher.update(buffer, 0, read, outBuffer);

			baos.write(outBuffer, 0, decrypted);
			// baos.write(buffer, 0, read);
			length -= read;

		}

		baos.write(cipher.doFinal());

		json = new String(baos.toByteArray());

		props.putAll(GSON.fromJson(json, MAP_STRING_STRING_TYPE));

		while (this.original.getFilePointer() < this.original.length())
		{
			baos.reset();
			final long offset = this.original.getFilePointer();
			final long metaLength = this.original.readLong();
			final long metaOffset = this.original.getFilePointer();

			length = metaLength;
			while (length > 0)
			{

				if (length < buffer.length)
					read = this.original.read(buffer, 0, (int) length);
				else
					read = this.original.read(buffer);

				final int decrypted = cipher.update(buffer, 0, read, outBuffer);
				baos.write(outBuffer, 0, decrypted);
				length -= read;
			}
			baos.write(cipher.doFinal());
			json = new String(baos.toByteArray());

			final Map<String, String> properties = new HashMap<>(GSON.fromJson(json, MAP_STRING_STRING_TYPE));
			final String path = properties.get(Block.PATH_LABEL);
			if (path == null)
				throw new IllegalStateException("Path of block starting at " + offset + " is not set");

			if (blocks.containsKey(path.toUpperCase(Environment.getLocale())))
				throw new IllegalStateException("Block path " + path + " already exist");

			final long dataLength = this.original.readLong();
			final long dataOffset = this.original.getFilePointer();

			this.root.mkdir(path, true);

			final String [] tokens = path.split(Folder.REGEX_DELIMITER);

			final org.ortis.jsafe.SafeFile dstFile;

			if (tokens.length == 2)
				dstFile = this.root;
			else
				dstFile = this.root.get(tokens, 1, tokens.length - 1);

			if (dstFile == null)
				throw new Exception("Could not find destination folder for block path " + path);

			if (!dstFile.isFolder())
				throw new Exception("Destination folder " + dstFile + " is a block");

			final Folder destinationFolder = ((Folder) dstFile);

			final Block block = new Block(path, properties, offset, dataOffset + dataLength - offset, metaOffset, metaLength, dataOffset, dataLength, destinationFolder);

			destinationFolder.add(block);

			blocks.put(block.getComparablePath(), block);
			this.original.seek(block.getOffset() + block.getLength());
		}

	}

	/**
	 * Add data into the {@link Safe}. <b>Note that the data will be stored into the temporary safe file</b>. Use {@link Safe#save()} to save all temporary data
	 * 
	 * @param properties:
	 *            metadata
	 * @param data:
	 *            data to encrypt
	 * @return
	 * @throws Exception
	 */
	public Block add(final Map<String, String> properties, final InputStream data,  TaskProbe probe) throws Exception
	{
		if(probe == null)
			probe = EMPTY_PROBE;

		try
		{
			final String path = properties.get(Block.PATH_LABEL);

			if (path == null)
				throw new IllegalArgumentException("Property " + Block.PATH_LABEL + " is missing");

			org.ortis.jsafe.SafeFile destinationFile = this.root.get(path);

			if (destinationFile != null)
				throw new Exception("Block file " + destinationFile + " already exist");

			final String comparablePath = properties.get(Block.PATH_LABEL).toUpperCase(Environment.getLocale());

			final String [] comparableTokens = comparablePath.split(Folder.REGEX_DELIMITER);

			if (comparableTokens.length == 2 && root.getComparableName().equals(comparableTokens[0]))
				destinationFile = this.root;
			else
				destinationFile = this.root.get(comparableTokens, 1, comparableTokens.length - 1);

			if (destinationFile == null)
				throw new Exception("Destination folder " + destinationFile + " does not exists");

			if (!destinationFile.isFolder())
				throw new Exception("Destination " + destinationFile + " is not a folder");

			final Folder destinationFolder = (Folder) destinationFile;

			if (destinationFolder.get(comparableTokens, comparableTokens.length - 1, comparableTokens.length) != null)
				throw new Exception("Block file " + path + " already exist");

			if (this.roBlocks.containsKey(path) || this.tempBlocks.containsKey(path))
				throw new Exception("Block path " + path + " already exist");

			final String name = properties.get(Block.NAME_LABEL);

			if (name == null)
				throw new IllegalArgumentException("Property " + Block.NAME_LABEL + " is missing");

			cipher.init(Cipher.ENCRYPT_MODE, keySpec, this.algoSpec);

			if (probe.isCancelRequested())
			{
				probe.fireCanceled();
				throw new CancellationException();
			}

			final String metadataserial = GSON.toJson(properties);

			final byte [] metaBuffer = metadataserial.getBytes();

			final RandomAccessFile temp = getTemp();

			final long offset = temp.getFilePointer();

			temp.writeLong(0);
			final long metaOffset = temp.getFilePointer();

			final long metaLength = encrypt(new ByteArrayInputStream(metaBuffer), this.cipher, temp, this.bufferSize, probe);

			long position = temp.getFilePointer();
			temp.seek(offset);
			temp.writeLong(metaLength);
			temp.seek(position);

			position = temp.getFilePointer();
			temp.writeLong(0);
			final long dataOffset = temp.getFilePointer();

			final long dataLength = encrypt(data, this.cipher, temp, this.bufferSize, probe);

			temp.seek(position);
			temp.writeLong(dataLength);

			temp.seek(temp.length());

			final Block block = new Block(path, properties, offset, temp.getFilePointer() - offset, metaOffset, metaLength, dataOffset, dataLength, destinationFolder);
			this.tempBlocks.put(block.getComparablePath(), block);

			destinationFolder.add(block);

			return block;

		} catch (final CancellationException e)
		{
			throw e;
		} catch (final Exception e)
		{
			probe.fireException(e);
			throw e;
		} finally
		{
			probe.fireTerminated();
		}
	}

	/**
	 * Delete data from the {@link Safe}. <b>Note that the data wont be deleted until a call to {@link Safe#save()} is made</b>
	 * 
	 * @param path:
	 *            path of the data to delete
	 */
	public void delete(final String path)
	{

		final String comparablePath = path.toUpperCase(Environment.getLocale());
		Block deleted = this.blocks.remove(comparablePath);
		if (deleted != null)
			this.deletedBlocks.put(comparablePath, deleted);

		deleted = this.tempBlocks.remove(comparablePath);

		if (deleted != null)
			this.deletedBlocks.put(comparablePath, deleted);
	}

	/**
	 * Extract data from the {@link Safe}
	 * 
	 * @param block:
	 *            block to extract
	 * @param outputStream:
	 *            destination of extracted block
	 * @throws Exception
	 */
	public void extract(final Block block, final OutputStream outputStream) throws Exception
	{
		extract(block.getPath(), outputStream);
	}

	/**
	 * Extract data from the {@link Safe}
	 * 
	 * @param path:
	 *            path of the block to extract
	 * @param outputStream:
	 *            destination of extracted block
	 * @throws Exception
	 */
	public void extract(String path, final OutputStream outputStream) throws Exception
	{
		path = path.toUpperCase(Environment.getLocale());

		Block block = this.roBlocks.get(path);

		final RandomAccessFile raf;
		if (block == null)
		{
			block = this.tempBlocks.get(path);
			raf = this.temp;
		} else
			raf = this.original;

		if (block == null)
			throw new Exception("Block " + path + " not found");

		raf.seek(block.getDataOffset());
		this.cipher.init(Cipher.DECRYPT_MODE, this.keySpec, this.algoSpec);
		decrypt(raf, block.getDataLength(), cipher, outputStream, this.bufferSize);

	}

	/**
	 * Read the metadata of a {@link Block}
	 * 
	 * @param block:
	 *            block to read
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> readMetadata(final Block block) throws Exception
	{

		this.original.seek(block.getMetaOffset());
		this.cipher.init(Cipher.DECRYPT_MODE, this.keySpec, this.algoSpec);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		decrypt(this.original, block.getMetaLength(), this.cipher, baos, this.bufferSize);

		final String metadata = new String(baos.toByteArray());

		final Map<String, String> jsonMap = GSON.fromJson(metadata, MAP_STRING_STRING_TYPE);
		return new TreeMap<>(jsonMap);
	}

	/**
	 * Discard pending modification
	 */
	public void discardChanges()
	{
		this.tempBlocks.clear();
		this.deletedBlocks.clear();
	}

	/**
	 * Save the modification into the safe file. The current file is renamed and a new file is written. This is to reduce the risk of data loss. This method calls the {@link Safe#close()} before returning
	 * 
	 * @return
	 * @throws Exception
	 */
	public Safe save() throws Exception
	{
		return save(EMPTY_PROBE);
	}

	public Safe save(final TaskProbe probe) throws Exception
	{

		try
		{
			double progress = 0;
			probe.fireProgress(progress);
			// add non deleted
			probe.fireMessage("Creating temporary file");
			final File newFile = Files.createTempFile(originalFile.getParentFile().toPath(), null, null).toFile();

			try (RandomAccessFile destination = new RandomAccessFile(newFile, "rw"))
			{
				this.cipher.init(Cipher.ENCRYPT_MODE, this.keySpec, this.algoSpec);

				if (probe.isCancelRequested())
				{
					probe.fireCanceled();
					throw new CancellationException();
				}

				// public properties
				probe.fireMessage("Writing public header");
				String json = GSON.toJson(this.publicHeader) + "\n";

				long previousPosition = destination.getFilePointer();
				destination.writeLong(0);
				long total = write(new ByteArrayInputStream(json.getBytes()), destination, this.bufferSize, probe);
				long position = destination.getFilePointer();
				destination.seek(previousPosition);
				destination.writeLong(total);
				destination.seek(position);

				if (probe.isCancelRequested())
				{
					probe.fireCanceled();
					throw new CancellationException();
				}

				// private properties
				probe.fireMessage("Writing private properties");
				json = GSON.toJson(this.privateProperties);

				previousPosition = destination.getFilePointer();
				destination.writeLong(0);
				total = encrypt(new ByteArrayInputStream(json.getBytes()), this.cipher, destination, this.bufferSize, probe);
				position = destination.getFilePointer();
				destination.seek(previousPosition);
				destination.writeLong(total);
				destination.seek(position);

				if (probe.isCancelRequested())
				{
					probe.fireCanceled();
					throw new CancellationException();
				}

				final double steps = this.roBlocks.size() + this.tempBlocks.size() + 1;
				int completed = 0;

				for (final Block block : this.roBlocks.values())
				{

					probe.fireMessage("Writing block " + block.getPath());
					this.original.seek(block.getOffset());
					write(this.original, block.getLength(), destination, this.bufferSize, probe);
					completed++;
					progress = completed / steps;
					probe.fireProgress(progress);
				}

				final RandomAccessFile temp = getTemp();
				for (final Block block : this.tempBlocks.values())
				{
					probe.fireMessage("Writing block " + block.getPath());
					temp.seek(block.getOffset());
					write(temp, block.getLength(), destination, this.bufferSize, probe);
					completed++;
					progress = completed / steps;
					probe.fireProgress(progress);

				}

				probe.fireMessage("Closing IO streams");
				destination.close();

				close();

				probe.fireMessage("Renaming files");
				if (!this.originalFile.renameTo(new File(this.originalFile.getAbsolutePath() + "." + FILE_NAME_FORMATTER.format(LocalDateTime.now()))))
					throw new IOException("Unable to rename " + this.originalFile.getAbsolutePath());

				if (!newFile.renameTo(this.originalFile))
					throw new IOException("Unable to rename " + newFile.getAbsolutePath());

				if (probe.isCancelRequested())
				{
					probe.fireCanceled();
					throw new CancellationException();
				}

				probe.fireMessage("Opening new safe");
				probe.fireProgress(1);

				return new Safe(this.originalFile, cipher, keySpec, this.algoSpec, this.bufferSize);
			}
		} catch (final CancellationException e)
		{
			throw e;
		} catch (final Exception e)
		{
			probe.fireException(e);
			throw e;
		} finally
		{
			probe.fireTerminated();
		}

	}

	@Override
	public void close() throws IOException
	{
		this.original.close();

		final RandomAccessFile temp = getTemp();
		if (temp != null)
		{
			temp.close();
			tempFile.delete();
		}

	}

	/**
	 * Get the properties of the {@link Safe}
	 * 
	 * @return
	 */
	public Map<String, String> getPrivateProperties()
	{
		return privateProperties;
	}

	/**
	 * Get the header of the {@link Safe}
	 * 
	 * @return
	 */
	public Map<String, String> getPublicHeader()
	{
		return publicHeader;
	}

	/**
	 * Get all {@link Block} contained in the {@link Safe}
	 * 
	 * @return
	 */
	public Map<String, Block> getBlocks()
	{
		return this.roBlocks;
	}

	/**
	 * Get a {@link Block} from the {@link Safe}
	 * 
	 * @param path:
	 *            path of the {@link Block} to retrieve
	 * @return
	 */
	public Block getBlock(final String path)
	{

		final String comparablePath = path.toUpperCase(Environment.getLocale());

		return this.roBlocks.get(comparablePath);

	}

	/**
	 * Get a {@link Block} from the temporary {@link Safe}
	 * 
	 * @param path:
	 *            path of the {@link Block} to retrieve
	 * @return
	 */
	public Block getTempBlock(final String path)
	{
		final String comparablePath = path.toUpperCase(Environment.getLocale());

		return this.tempBlocks.get(comparablePath);

	}

	/**
	 * Get all {@link Block} contained in the temporary {@link Safe}
	 * 
	 * @return
	 */
	public Map<String, Block> getTempBlocks()
	{
		return tempBlocks;
	}

	/**
	 * Get deleted {@link Block}
	 * 
	 * @return
	 */
	public Map<String, Block> getDeletedBlocks()
	{
		return deletedBlocks;
	}

	/**
	 * Get root {@link Folder}
	 * 
	 * @return
	 */
	public Folder getRootFolder()
	{
		return root;
	}

	/**
	 * Get the temporary safe file
	 * 
	 * @return
	 */
	public File getTempFile()
	{
		return tempFile;
	}

	/**
	 * Get the temporary safe file
	 * 
	 * @return
	 */
	public RandomAccessFile getTemp() throws IOException
	{/*
		if (this.temp == null)
		{
		
			if (this.tempFile.exists())
				throw new IOException("File " + this.tempFile + " already exist");
		
			this.temp = new RandomAccessFile(this.tempFile, "rw");
			this.tempFile.deleteOnExit();
		
		}
		*/
		return this.temp;
	}

	private static long encrypt(final InputStream data, final Cipher cipher, final RandomAccessFile destination, final int bufferSize, final TaskProbe probe) throws Exception
	{

		final byte [] buffer = new byte[bufferSize];
		final byte [] bufferOut = new byte[bufferSize];
		// ByteBuffer in;

		// final ByteBuffer out = ByteBuffer.allocateDirect(buffer.length);

		long total = 0;
		int read;
		while ((read = data.read(buffer)) > -1)
		{

			read = cipher.update(buffer, 0, read, bufferOut);
			total += read;
			destination.write(bufferOut, 0, read);

			if (probe.isCancelRequested())
			{
				probe.fireCanceled();
				throw new CancellationException();
			}

		}

		read = cipher.doFinal(bufferOut, 0);
		destination.write(bufferOut, 0, read);
		total += read;

		return total;

	}

	private static void decrypt(final RandomAccessFile source, final long length, final Cipher cipher, final OutputStream destination, final int bufferSize) throws Exception
	{

		final byte [] buffer = new byte[bufferSize];
		final byte [] bufferOut = new byte[bufferSize];
		// ByteBuffer in;

		// final ByteBuffer out = ByteBuffer.allocateDirect(buffer.length);
		long remaining = length;
		int read;
		while (remaining > 0)
		{
			if (remaining < buffer.length)
				read = source.read(buffer, 0, (int) remaining);
			else
				read = source.read(buffer, 0, buffer.length);

			remaining -= read;

			read = cipher.update(buffer, 0, read, bufferOut);
			destination.write(bufferOut, 0, read);

		}

		read = cipher.doFinal(bufferOut, 0);
		destination.write(bufferOut, 0, read);

	}

	private static long write(final InputStream data, final RandomAccessFile destination, final int bufferSize, final TaskProbe probe) throws Exception
	{

		final byte [] buffer = new byte[bufferSize];

		long total = 0;
		int read;
		while ((read = data.read(buffer)) > -1)
		{
			destination.write(buffer, 0, read);
			total += read;

			if (probe.isCancelRequested())
			{
				probe.fireCanceled();
				throw new CancellationException();
			}
		}

		return total;

	}

	private static void write(final RandomAccessFile source, final long length, final RandomAccessFile destination, final int bufferSize, final TaskProbe probe) throws Exception
	{

		final byte [] buffer = new byte[bufferSize];

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

			if (probe.isCancelRequested())
			{
				probe.fireCanceled();
				throw new CancellationException();
			}
		}

	}

	/**
	 * Read the header of the {@link Safe}
	 * 
	 * @param file:
	 *            safe file to read
	 * @param bufferSize:
	 *            size of the <code>byte</code> buffer to be used in IO operation
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> readHeader(final File file, final int bufferSize) throws IOException
	{
		RandomAccessFile raf = null;

		try
		{
			raf = new RandomAccessFile(file, "rw");
			final byte [] buffer = new byte[bufferSize];
			final ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);

			long length = raf.readLong();
			int read;
			while (length > 0)

			{

				if (length < buffer.length)
					read = raf.read(buffer, 0, (int) length);
				else
					read = raf.read(buffer);

				baos.write(buffer, 0, read);
				length -= read;

			}
			final String header = new String(baos.toByteArray(), UTF8);
			return GSON.fromJson(header, MAP_STRING_STRING_TYPE);
		} finally
		{
			if (raf != null)
				raf.close();
		}

	}

	/**
	 * Create a new {@link Safe}
	 * 
	 * @param file
	 * @param key
	 * @param publicHeader
	 * @param privateProperties
	 * @param bufferSize
	 * @return
	 * @throws Exception
	 */
	public static Safe create(final File file, final byte [] key, final Map<String, String> publicHeader, final Map<String, String> privateProperties, final int bufferSize) throws Exception
	{

		final String encryption = publicHeader.get(ENCRYPTION_LABEL);

		if (encryption == null)
			throw new Exception("Public property '" + ENCRYPTION_LABEL + "' must be set");

		Cipher cipher = javax.crypto.Cipher.getInstance(encryption);

		final String keyAlgo = publicHeader.get(KEY_ALGO_LABEL);
		if (keyAlgo == null)
			throw new Exception("Public property '" + KEY_ALGO_LABEL + "' must be set");

		final IvParameterSpec iv;

		if (publicHeader.containsKey(ENCRYPTION_IV_LABEL))
			iv = new IvParameterSpec(GSON.fromJson(publicHeader.get(ENCRYPTION_IV_LABEL), BYTE_ARRAY_TYPE));
		else
			iv = null;

		final SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgo);

		cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

		if (file.exists())
			throw new IOException("File " + file + " already exist");

		if (!file.createNewFile())
			throw new IOException("Could not create file " + file);

		final RandomAccessFile raf = new RandomAccessFile(file, "rw");

		long total, position, previousPosition;
		// header
		final String header = GSON.toJson(publicHeader) + "\n";

		previousPosition = raf.getFilePointer();
		raf.writeLong(0);
		total = write(new ByteArrayInputStream(header.getBytes(UTF8)), raf, bufferSize, EMPTY_PROBE);
		position = raf.getFilePointer();
		raf.seek(previousPosition);
		raf.writeLong(total);
		raf.seek(position);

		// properties
		final String privatePropsJson = GSON.toJson(privateProperties == null ? new HashMap<>() : privateProperties);
		previousPosition = raf.getFilePointer();
		raf.writeLong(0L);
		total = encrypt(new ByteArrayInputStream(privatePropsJson.getBytes(UTF8)), cipher, raf, bufferSize, EMPTY_PROBE);
		position = raf.getFilePointer();
		raf.seek(previousPosition);
		raf.writeLong(total);
		raf.seek(position);

		raf.close();

		return new Safe(file, cipher, keySpec, iv, bufferSize);

	}

}
