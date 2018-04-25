/*******************************************************************************
 * Copyright 2018 Ortis (cao.ortis.org@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.ortis.jsafe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.security.spec.AlgorithmParameterSpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Safe
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

	public final static String ENCRYPTION_LABEL = "encryption";
	public final static String ENCRYPTION_IV_LABEL = "iv";
	public final static String KEY_ALGO_LABEL = "algo";
	public final static String USER_MANUAL_LABEL = "protocol description";

	public final static String USER_MANUAL = "lets do it her\nhahaha\ndone";

	private final static String TEMP_EXTENSION = "~";
	private final static String NEW_EXTENSION = "~~";

	private final File originalFile;

	private final Cipher cipher;
	private final SecretKeySpec keySpec;
	private final AlgorithmParameterSpec algoSpec;

	private final RandomAccessFile original;

	private final File tempFile;
	private RandomAccessFile temp;

	private final Map<String, String> publicProperties;
	private final Map<String, String> privateProperties;
	private final Map<String, Block> blocks;
	private final Map<String, Block> tempBlocks;
	private final Map<String, Block> deletedBlocks;

	private final int bufferSize;

	private final Folder root;

	public Safe(final File file, final Cipher cipher, final SecretKeySpec keySpec, final AlgorithmParameterSpec algoSpec, final int bufferSize) throws Exception
	{

		this.originalFile = file;
		this.cipher = cipher;

		this.keySpec = keySpec;
		this.algoSpec = algoSpec;
		this.bufferSize = bufferSize;

		this.cipher.init(Cipher.DECRYPT_MODE, this.keySpec, this.algoSpec);

		this.original = new RandomAccessFile(file, "rw");

		this.tempFile = new File(file.getAbsolutePath() + TEMP_EXTENSION);

		final HashMap<String, String> publicProps = new HashMap<>();
		this.publicProperties = Collections.unmodifiableMap(publicProps);

		final HashMap<String, String> props = new HashMap<>();
		this.privateProperties = Collections.unmodifiableMap(props);
		final HashMap<String, Block> blocks = new HashMap<>();
		this.blocks = Collections.unmodifiableMap(blocks);
		this.tempBlocks = new HashMap<>();

		this.deletedBlocks = new HashMap<>();

		this.root = new Folder(null, Folder.ROOT_NAME);

		final byte [] buffer = new byte[bufferSize];
		final byte [] outBuffer = new byte[bufferSize];

		// read public Properties
		/*
		long length = this.original.readLong();
		
		int read;
		
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);
		
		while (length > 0)
		{
		
			if (length < buffer.length)
				read = this.original.read(buffer, 0, (int) length);
			else
				read = this.original.read(buffer);
		
			baos.write(buffer, 0, read);
			length -= read;
		
		}
		*/

		long length;

		int read;

		final ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);

		final long headerLength = this.original.readLong();
		// byte c = this.original.readByte();
		// while (c != '\n' && c != '\r')
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

			final long dataLength = this.original.readLong();
			final long dataOffset = this.original.getFilePointer();
			final Block block = new Block(path, properties, offset, dataOffset + dataLength - offset, metaOffset, metaLength, dataOffset, dataLength);

			final String [] tokens = path.split(Folder.REGEX_DELIMITER);
			// final String [] comparableTokens = path.split(Folder.REGEX_DELIMITER);

			if (blocks.containsValue(block))
				throw new IllegalStateException("Block path " + block.getPath() + " already exist");

			this.root.mkdir(path, true);

			final org.ortis.jsafe.SafeFile dstFile;

			if (tokens.length == 2)
				dstFile = this.root;
			else
				dstFile = this.root.get(tokens, 1, tokens.length - 1);

			if (dstFile == null)
				throw new Exception("Could not find destination folder for block " + block);

			if (!dstFile.isFolder())
				throw new Exception("Destination folder " + dstFile + " is a block");

			((Folder) dstFile).add(block);

			blocks.put(block.getComparablePath(), block);
			this.original.seek(block.getOffset()+block.getLength());
		}

	}

	public void add(final Map<String, String> properties, final InputStream data) throws Exception
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

		if (this.blocks.containsKey(path) || this.tempBlocks.containsKey(path))
			throw new Exception("Block path " + path + " already exist");

		final String name = properties.get(Block.NAME_LABEL);

		if (name == null)
			throw new IllegalArgumentException("Property " + Block.NAME_LABEL + " is missing");

		cipher.init(Cipher.ENCRYPT_MODE, keySpec, this.algoSpec);

		final String metadataserial = GSON.toJson(properties);

		final byte [] metaBuffer = metadataserial.getBytes();

		final RandomAccessFile temp = getTemp();

		final long offset = temp.getFilePointer();

		temp.writeLong(0);
		final long metaOffset = temp.getFilePointer();

		long total = encrypt(new ByteArrayInputStream(metaBuffer), this.cipher, temp, this.bufferSize);
		long position = temp.getFilePointer();
		temp.seek(offset);
		temp.writeLong(total);
		temp.seek(position);

		position = temp.getFilePointer();
		temp.writeLong(0);
		final long dataOffset = temp.getFilePointer();

		total = encrypt(data, this.cipher, temp, this.bufferSize);

		temp.seek(position);
		temp.writeLong(total);

		temp.seek(temp.length());

		final Block block = new Block(path, properties, offset, temp.getFilePointer() - offset, metaOffset, metaBuffer.length, dataOffset, total);
		this.tempBlocks.put(block.getComparablePath(), block);

		destinationFolder.add(block);
	}

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

	public void extract(String path, final OutputStream outputStream) throws Exception
	{
		path = path.toUpperCase(Environment.getLocale());

		Block block = this.blocks.get(path);

		if (block == null)
			block = this.tempBlocks.get(path);

		if (block == null)
			throw new Exception("Block " + path + " not found");

		this.original.seek(block.getDataOffset());
		this.cipher.init(Cipher.DECRYPT_MODE, this.keySpec, this.algoSpec);
		decrypt(this.original, block.getDataLength(), cipher, outputStream, this.bufferSize);

	}

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

	public Safe save() throws Exception
	{

		// add non deleted
		final File newFile = new File(originalFile.getAbsolutePath() + NEW_EXTENSION);
		if (newFile.exists())
			throw new IOException("File " + newFile + " already exist");

		final RandomAccessFile destination = new RandomAccessFile(newFile, "rw");

		this.cipher.init(Cipher.ENCRYPT_MODE, this.keySpec, this.algoSpec);
		// public properties

		String json = GSON.toJson(this.publicProperties) + "\n";

		long previousPosition = destination.getFilePointer();
		destination.writeLong(0);
		long total = write(new ByteArrayInputStream(json.getBytes()), destination, this.bufferSize);
		long position = destination.getFilePointer();
		destination.seek(previousPosition);
		destination.writeLong(total);
		destination.seek(position);

		// private properties
		json = GSON.toJson(this.privateProperties);

		previousPosition = destination.getFilePointer();
		destination.writeLong(0);
		total = encrypt(new ByteArrayInputStream(json.getBytes()), this.cipher, destination, this.bufferSize);
		position = destination.getFilePointer();
		destination.seek(previousPosition);
		destination.writeLong(total);
		destination.seek(position);

		for (final Block block : this.blocks.values())
		{
			this.original.seek(block.getOffset());
			write(this.original, block.getLength(), destination, this.bufferSize);

		}

		final RandomAccessFile temp = getTemp();
		for (final Block block : this.tempBlocks.values())
		{
			temp.seek(block.getOffset());
			write(temp, block.getLength(), destination, this.bufferSize);

		}

		destination.close();

		closeIO();

		this.originalFile.renameTo(new File(this.originalFile.getAbsolutePath() + "." + FILE_NAME_FORMATTER.format(LocalDateTime.now())));

		newFile.renameTo(this.originalFile);

		return new Safe(this.originalFile, cipher, keySpec, this.algoSpec, this.bufferSize);

	}

	private void closeIO() throws IOException
	{

		this.original.close();
		this.original.close();
		final RandomAccessFile temp = getTemp();
		if (temp != null)
		{
			temp.close();
			tempFile.delete();
		}

	}

	public Map<String, String> getPrivateProperties()
	{
		return privateProperties;
	}

	public Map<String, String> getPublicProperties()
	{
		return publicProperties;
	}

	public Map<String, Block> getBlocks()
	{
		return this.blocks;
	}

	public Block getBlock(final String path)
	{

		final String comparablePath = path.toUpperCase(Environment.getLocale());

		return this.blocks.get(comparablePath);

	}

	public Block getTempBlock(final String path)
	{
		final String comparablePath = path.toUpperCase(Environment.getLocale());

		return this.tempBlocks.get(comparablePath);

	}

	public Map<String, Block> getTempBlocks()
	{
		return tempBlocks;
	}

	public Map<String, Block> getDeletedBlocks()
	{
		return deletedBlocks;
	}

	public Folder getRootFolder()
	{
		return root;
	}

	public File getTempFile()
	{
		return tempFile;
	}

	public RandomAccessFile getTemp() throws IOException
	{
		if (this.temp == null)
		{

			if (this.tempFile.exists())
				throw new IOException("File " + this.tempFile + " already exist");

			this.tempFile.deleteOnExit();

			this.temp = new RandomAccessFile(this.tempFile, "rw");

		}

		return temp;
	}

	@Override
	protected void finalize() throws Throwable
	{
		closeIO();

		super.finalize();
	}

	private static long encrypt(final InputStream data, final Cipher cipher, final RandomAccessFile destination, final int bufferSize) throws Exception
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
			// destination.write(buffer, 0, read);

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

	private static long write(final InputStream data, final RandomAccessFile destination, final int bufferSize) throws IOException
	{

		final byte [] buffer = new byte[bufferSize];

		long total = 0;
		int read;
		while ((read = data.read(buffer)) > -1)
		{
			destination.write(buffer, 0, read);
			total += read;
		}

		return total;

	}

	private static void write(final RandomAccessFile source, final long length, final RandomAccessFile destination, final int bufferSize) throws IOException
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
		}

	}

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

	public static Safe create(final File file, final byte [] key, final Map<String, String> publicHeader, final Map<String, String> privateProperties, final int bufferSize) throws Exception
	{

		publicHeader.put(USER_MANUAL_LABEL, USER_MANUAL);

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
		// raf.write(header.getBytes(UTF8));
		total = write(new ByteArrayInputStream(header.getBytes(UTF8)), raf, bufferSize);
		position = raf.getFilePointer();
		raf.seek(previousPosition);
		raf.writeLong(total);
		raf.seek(position);

		// properties
		final String privatePropsJson = GSON.toJson(privateProperties == null ? new HashMap<>() : privateProperties);
		previousPosition = raf.getFilePointer();
		raf.writeLong(0L);
		total = encrypt(new ByteArrayInputStream(privatePropsJson.getBytes(UTF8)), cipher, raf, bufferSize);
		position = raf.getFilePointer();
		raf.seek(previousPosition);
		raf.writeLong(total);
		raf.seek(position);

		raf.close();

		return new Safe(file, cipher, keySpec, iv, bufferSize);

	}

}
