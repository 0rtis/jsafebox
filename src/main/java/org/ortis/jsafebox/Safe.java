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

package org.ortis.jsafebox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.ortis.jsafebox.hash.Hasher;
import org.ortis.jsafebox.hash.SHA256;
import org.ortis.jsafebox.task.TaskProbe;

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
	public static final String VERSION = "0.2 beta";

	public static final Gson GSON = new Gson();
	private static final Type MAP_STRING_STRING_TYPE = new TypeToken<Map<String, String>>()
	{
	}.getType();

	public static final Type BYTE_ARRAY_TYPE = new TypeToken<byte []>()
	{
	}.getType();

	private final static Hasher HASHER = new SHA256();

	public static final String ENCRYPTION_LABEL = "encryption";
	public final static String ENCRYPTION_IV_LENGTH_LABEL = "iv length";
	public static final String KEY_ALGO_LABEL = "algo";
	public static final String PROTOCOL_SPEC_LABEL = "protocol description";
	public static final String PBKDF2_SALT_LABEL = "pbkdf2 salt";
	public static final int PBKDF2_ITERATION = 100000;
	public static final String PBKDF2_ITERATION_LABEL = "pbkdf2 iteration";

	public static final String PROTOCOL_SPEC = "JSafebox is using a very simple protocol so encrypted files can be easily read by another program, as long as you have the password. The encryption key is derived from the password using PBKDF2 hashing with 100000 iteration. A JSafebox file contains a SHA256 integrity hash followed by blocks: [ integrity hash | block 0 | block 1 | ... | block N ]. Each block is stored as followed: [ IV | metadata length | metadata | data length | data ] where 'IV' is the Initialization_vector of the encryption (16 bytes), 'metadata' is a JSON string and 'length' are 64 bits (8 bytes) integer. The first block 'block 0' is the 'header' and is the only block not encrypted and therefore, the only block without IV. The 'header' only have metadata ('data length' is 0) and contains text entries specified by the user and various additional entries including a protocol explanation, the type of encoding and the parameters of the encryption. The 'header's metadata is stored as JSON string and can be seen by opening the safe file with a basic text editor. The second block 'block 1' is the 'properties'. It is similar to the 'header' except that it is encrypted and have an IV. The 'properties' contains text entries specified by the user and stored in JSON. The following blocks (from 2 to N) are the encrypted files. (Full manual at https://github.com/0rtis/jsafebox)";

	private final File originalFile;

	private final SecretKey encryptionKey;
	private final int ivLength;
	private final RandomAccessFile original;

	private final File tempFile;
	private final RandomAccessFile temp;

	private final byte [] hash;

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
	public Safe(final File file, final SecretKey key, final int bufferSize) throws Exception
	{
		// Initialize variables
		this.originalFile = file.getAbsoluteFile();
		this.encryptionKey = key;
		this.bufferSize = bufferSize;
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
		final byte [] bufferDecrypted = new byte[bufferSize];// Hold decrypted data

		this.original.read(buffer, 0, HASHER.getHashLength());
		this.hash = new byte[HASHER.getHashLength()];
		System.arraycopy(buffer, 0, this.hash, 0, this.hash.length);

		final TaskProbe probe = TaskProbe.DULL_PROBE;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(buffer.length);

		// Read header
		SafeIO.write(this.original, this.original.readLong(), baos, buffer, probe);
		String json = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		publicProps.putAll(GSON.fromJson(json, MAP_STRING_STRING_TYPE));
		this.original.readLong();// skip data length 0

		// Initialize cipher
		this.ivLength = Integer.parseInt(this.publicHeader.get(ENCRYPTION_IV_LENGTH_LABEL));
		final Cipher cipher = getCipher();

		// Read private properties
		this.original.read(buffer, 0, this.ivLength);// read properties iv
		IvParameterSpec iv = new IvParameterSpec(Arrays.copyOf(buffer, this.ivLength));
		cipher.init(Cipher.DECRYPT_MODE, this.encryptionKey, iv);
		baos.reset();
		SafeIO.decrypt(this.original, this.original.readLong(), cipher, baos, buffer, bufferDecrypted);
		json = new String(baos.toByteArray());
		props.putAll(GSON.fromJson(json, MAP_STRING_STRING_TYPE));
		this.original.readLong();// data length 0

		// Read blocks
		while (this.original.getFilePointer() < this.original.length())
		{
			baos.reset();

			final long offset = this.original.getFilePointer();

			this.original.read(buffer, 0, this.ivLength);// read properties iv
			iv = new IvParameterSpec(Arrays.copyOf(buffer, this.ivLength));
			cipher.init(Cipher.DECRYPT_MODE, this.encryptionKey, iv);

			final long metaLength = this.original.readLong();
			final long metaOffset = this.original.getFilePointer();

			SafeIO.decrypt(this.original, metaLength, cipher, baos, buffer, bufferDecrypted);
			json = new String(baos.toByteArray());

			final Map<String, String> properties = new HashMap<>(GSON.fromJson(json, MAP_STRING_STRING_TYPE));
			final String path = properties.get(Block.PATH_LABEL);
			if (path == null)
				throw new IllegalStateException("Path of block starting at " + offset + " is not set");

			if (this.blocks.containsKey(path.toUpperCase(Environment.getLocale())))
				throw new IllegalStateException("Block path " + path + " already exist");

			final long dataLength = this.original.readLong();
			final long dataOffset = this.original.getFilePointer();
			final long blockLength = this.original.getFilePointer() - offset + dataLength;

			final String [] tokens = path.split(Folder.REGEX_DELIMITER);
			this.root.mkdir(tokens, 1, true);

			final org.ortis.jsafebox.SafeFile dstFile;

			if (tokens.length == 2)
				dstFile = this.root;
			else
				dstFile = this.root.get(tokens, 1, tokens.length - 1);

			if (dstFile == null)
				throw new Exception("Could not find destination folder for block path " + path);

			if (!dstFile.isFolder())
				throw new Exception("Destination folder " + dstFile + " is a block");

			final Folder destinationFolder = ((Folder) dstFile);
			final Block block = new Block(path, properties, offset, blockLength, metaOffset, metaLength, dataOffset, dataLength, destinationFolder);
			destinationFolder.add(block);

			this.blocks.put(block.getComparablePath(), block);
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
	public synchronized Block add(final Map<String, String> properties, final InputStream data, TaskProbe probe) throws Exception
	{
		if (probe == null)
			probe = TaskProbe.DULL_PROBE;

		try
		{
			// Check inputs
			final String path = properties.get(Block.PATH_LABEL);

			if (path == null)
				throw new IllegalArgumentException("Property " + Block.PATH_LABEL + " is missing");

			org.ortis.jsafebox.SafeFile destinationFile = SafeFiles.get(path, this.root, this.root);

			if (destinationFile != null)
				throw new Exception("Block file " + destinationFile + " already exist");

			final String comparablePath = properties.get(Block.PATH_LABEL).toUpperCase(Environment.getLocale());

			final String [] comparableTokens = comparablePath.split(Folder.REGEX_DELIMITER);

			if (comparableTokens.length == 2 && root.getComparableName().equals(comparableTokens[0]))
				destinationFile = this.root;
			else
				destinationFile = this.root.get(comparableTokens, 1, comparableTokens.length - 1);

			if (destinationFile == null)
				throw new Exception("Destination folder not found");

			if (!destinationFile.isFolder())
				throw new Exception("Destination " + destinationFile + " is not a folder");

			final Folder destinationFolder = (Folder) destinationFile;

			if (this.roBlocks.containsKey(path) || this.tempBlocks.containsKey(path))
				throw new Exception("Block path " + path + " already exist");

			final String name = properties.get(Block.NAME_LABEL);

			if (name == null)
				throw new IllegalArgumentException("Property " + Block.NAME_LABEL + " is missing");

			probe.checkCancel();

			// Initialize Cipher, io buffers and temporary file
			final Cipher cipher = getCipher();
			cipher.init(Cipher.ENCRYPT_MODE, this.encryptionKey, getSecureRandom());

			final byte [] buffer = new byte[this.bufferSize];
			final byte [] bufferDecrypted = new byte[this.bufferSize];

			final RandomAccessFile temp = getTemp();

			final long offset = temp.getFilePointer();

			temp.write(cipher.getIV());

			// Write block's metadata
			temp.writeLong(0);
			final String metadataserial = GSON.toJson(properties);
			final byte [] metaBuffer = metadataserial.getBytes();
			final long metaOffset = temp.getFilePointer();
			final long metaLength = SafeIO.encrypt(new ByteArrayInputStream(metaBuffer), cipher, temp, buffer, bufferDecrypted, probe);
			long position = temp.getFilePointer();

			temp.seek(offset + cipher.getIV().length);
			temp.writeLong(metaLength);
			temp.seek(position);

			// Write block's data
			position = temp.getFilePointer();
			temp.writeLong(0);

			final long dataOffset = temp.getFilePointer();

			final long dataLength = SafeIO.encrypt(data, cipher, temp, buffer, bufferDecrypted, probe);

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
	public synchronized void delete(final String path)
	{

		final String comparablePath = path.toUpperCase(Environment.getLocale());
		Block deleted = this.blocks.get(comparablePath);

		if (deleted != null)
		{
			final Folder folder = deleted.getParent();
			folder.remove(deleted.getName());
			this.deletedBlocks.put(comparablePath, deleted);
		}

		deleted = this.tempBlocks.remove(comparablePath);

		if (deleted != null)
		{
			final Folder folder = deleted.getParent();
			folder.remove(deleted.getName());
			this.deletedBlocks.put(comparablePath, deleted);
		}
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
	public synchronized void extract(String path, final OutputStream outputStream) throws Exception
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

		raf.seek(block.getOffset());

		final byte [] ivBytes = new byte[this.ivLength];
		raf.read(ivBytes);

		final Cipher cipher = getCipher();

		final IvParameterSpec iv = new IvParameterSpec(ivBytes);

		cipher.init(Cipher.DECRYPT_MODE, this.encryptionKey, iv);
		raf.seek(block.getDataOffset());
		SafeIO.decrypt(raf, block.getDataLength(), cipher, outputStream, new byte[this.bufferSize], new byte[this.bufferSize]);
	}

	/**
	 * Read the metadata of a {@link Block}
	 * 
	 * @param block:
	 *            block to read
	 * @return
	 * @throws Exception
	 */
	public synchronized Map<String, String> readMetadata(final Block block) throws Exception
	{
		this.original.seek(block.getOffset());
		final byte [] ivBytes = new byte[this.ivLength];
		this.original.read(ivBytes);

		final Cipher cipher = getCipher();

		final IvParameterSpec iv = new IvParameterSpec(ivBytes);
		cipher.init(Cipher.DECRYPT_MODE, this.encryptionKey, iv);

		this.original.seek(block.getMetaOffset());

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SafeIO.decrypt(this.original, block.getMetaLength(), cipher, baos, new byte[this.bufferSize], new byte[this.bufferSize]);

		final String metadata = new String(baos.toByteArray());

		final Map<String, String> jsonMap = GSON.fromJson(metadata, MAP_STRING_STRING_TYPE);
		return new TreeMap<>(jsonMap);
	}

	/**
	 * Discard pending modification
	 */
	public synchronized void discardChanges() throws Exception
	{
		for (final Map.Entry<String, Block> temp : this.tempBlocks.entrySet())
		{
			Folder folder = temp.getValue().getParent();
			folder.remove(temp.getValue().getName());
		}

		this.tempBlocks.clear();

		for (final Map.Entry<String, Block> deleted : this.deletedBlocks.entrySet())
		{
			Folder folder = deleted.getValue().getParent();
			folder.add(deleted.getValue());
		}

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
		return save(null);
	}

	public synchronized Safe save(TaskProbe probe) throws Exception
	{
		if (probe == null)
			probe = TaskProbe.DULL_PROBE;
		try
		{
			double progress = 0;
			probe.fireProgress(progress);

			probe.fireMessage("Creating temporary file");
			final File newFile = Files.createTempFile(originalFile.getParentFile().toPath(), null, null).toFile();

			try (RandomAccessFile destination = new RandomAccessFile(newFile, "rw"))
			{

				final byte [] buffer = new byte[this.bufferSize];
				final byte [] bufferDecrypted = new byte[this.bufferSize];

				Cipher cipher = getCipher();

				probe.checkCancel();

				destination.write(HASHER.getEmptyHash());// skip hash

				// public properties
				probe.fireMessage("Writing public header");

				String json = GSON.toJson(this.publicHeader);

				long previousPosition = destination.getFilePointer();

				destination.writeLong(0);
				long total = SafeIO.write(new ByteArrayInputStream(json.getBytes()), destination, buffer, probe);
				destination.writeLong(0);// no data in header
				long position = destination.getFilePointer();
				destination.seek(previousPosition);
				destination.writeLong(total);
				destination.seek(position);

				probe.checkCancel();

				// private properties
				probe.fireMessage("Writing private properties");

				cipher.init(Cipher.ENCRYPT_MODE, this.encryptionKey, getSecureRandom());
				destination.write(cipher.getIV());

				json = GSON.toJson(this.privateProperties);

				previousPosition = destination.getFilePointer();
				destination.writeLong(0);
				total = SafeIO.encrypt(new ByteArrayInputStream(json.getBytes()), cipher, destination, buffer, bufferDecrypted, probe);
				destination.writeLong(0);// no data in header
				position = destination.getFilePointer();
				destination.seek(previousPosition);
				destination.writeLong(total);
				destination.seek(position);

				probe.checkCancel();

				final double steps = this.roBlocks.size() + this.tempBlocks.size() + 1;
				int completed = 0;

				for (final Block block : this.roBlocks.values())
				{
					// only add non deleted block
					if (this.deletedBlocks.containsKey(block.getComparablePath()))
					{
						probe.fireMessage("Skipping deleted block " + block.getPath());
						continue;
					}

					probe.fireMessage("Writing block " + block.getPath());
					this.original.seek(block.getOffset());
					SafeIO.write(this.original, block.getLength(), destination, buffer, probe);
					completed++;
					progress = completed / steps;
					probe.fireProgress(progress);
				}

				final RandomAccessFile temp = getTemp();
				for (final Block block : this.tempBlocks.values())
				{
					if (this.deletedBlocks.containsKey(block.getComparablePath()))
					{
						probe.fireMessage("Skipping deleted block " + block.getPath());
						continue;
					}

					probe.fireMessage("Writing block " + block.getPath());
					temp.seek(block.getOffset());

					SafeIO.write(temp, block.getLength(), destination, buffer, probe);
					completed++;
					progress = completed / steps;
					probe.fireProgress(progress);
				}

				probe.fireMessage("Computing hash");
				final byte [] hash = computeHash(destination, cipher, this.ivLength, this.encryptionKey, this.bufferSize, probe);
				destination.seek(0);
				destination.write(hash);

				probe.fireMessage("Closing IO streams");
				destination.close();

				close();

				probe.fireMessage("Deleting old file");

				if (!this.originalFile.delete())
					throw new IOException("Unable to delete " + this.originalFile.getAbsolutePath());

				probe.fireMessage("Renaming file");

				if (!newFile.renameTo(this.originalFile))
					throw new IOException("Unable to rename " + newFile.getAbsolutePath());

				probe.checkCancel();

				probe.fireMessage("Opening new safe");
				probe.fireProgress(1);

				return new Safe(this.originalFile, encryptionKey, this.bufferSize);
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

	/**
	 * Compute the hash of the {@link Safe}
	 * 
	 * @return
	 * @throws Exception
	 */
	public synchronized byte [] computeHash(final TaskProbe probe) throws Exception
	{
		final byte [] hash = computeHash(this.original, getCipher(), this.ivLength, this.encryptionKey, this.bufferSize, probe);
		return hash;
	}

	/**
	 * Return a copy of the hash that was in the {@link Safe}'s file
	 * 
	 * @return
	 */
	public byte [] getHash()
	{
		final byte [] destination = new byte[this.hash.length];
		System.arraycopy(this.hash, 0, destination, 0, this.hash.length);
		return destination;
	}

	private Cipher getCipher() throws Exception
	{
		final String encryption = this.publicHeader.get(ENCRYPTION_LABEL);
		if (encryption == null)
			throw new Exception("Public property '" + ENCRYPTION_LABEL + "' must be set");
		return javax.crypto.Cipher.getInstance(encryption);
	}

	@Override
	public synchronized void close() throws IOException
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

	public File getFile()
	{
		return this.originalFile;
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
	{
		return this.temp;
	}

	private static SecureRandom getSecureRandom()
	{
		return new SecureRandom();
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

			raf.read(buffer, 0, HASHER.getHashLength());// skip hash

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
			final String header = new String(baos.toByteArray(), StandardCharsets.UTF_8);
			return GSON.fromJson(header, MAP_STRING_STRING_TYPE);

		} finally
		{
			if (raf != null)
				raf.close();
		}
	}

	/**
	 * Compute the hash value of {@link Safe} file
	 * 
	 * @param safeFile
	 * @param cipher
	 * @param ivLength
	 * @param encryptionKey
	 * @param bufferSize
	 * @return
	 * @throws Exception
	 */
	public static byte [] computeHash(final RandomAccessFile safeFile, final Cipher cipher, final int ivLength, final Key encryptionKey, final int bufferSize, TaskProbe probe) throws Exception
	{
		if (probe == null)
			probe = TaskProbe.DULL_PROBE;

		try
		{
			final long previousPosition = safeFile.getFilePointer();

			final byte [] buffer = new byte[bufferSize];
			final byte [] bufferDecrypted = new byte[bufferSize];

			safeFile.seek(HASHER.getHashLength());
			final ByteBuffer byteBuffer = ByteBuffer.allocate((int) (safeFile.length() - safeFile.getFilePointer()));
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();

			long length = safeFile.readLong();
			byteBuffer.putLong(length);

			// header
			baos.reset();
			SafeIO.write(safeFile, length, baos, buffer, probe);
			byteBuffer.put(baos.toByteArray());
			byteBuffer.putLong(safeFile.readLong());// header's data length 0

			probe.checkCancel();

			// properties
			safeFile.read(buffer, 0, ivLength);// read properties iv
			byteBuffer.put(buffer, 0, ivLength);

			IvParameterSpec iv = new IvParameterSpec(Arrays.copyOf(buffer, ivLength));
			cipher.init(Cipher.DECRYPT_MODE, encryptionKey, iv);

			length = safeFile.readLong();
			byteBuffer.putLong(length);

			baos.reset();
			SafeIO.decrypt(safeFile, length, cipher, baos, buffer, bufferDecrypted);
			byteBuffer.put(baos.toByteArray());
			byteBuffer.putLong(safeFile.readLong());// properties data length 0

			probe.checkCancel();

			// blocks
			while (safeFile.getFilePointer() < safeFile.length())
			{
				safeFile.read(buffer, 0, ivLength);// read properties iv
				byteBuffer.put(buffer, 0, ivLength);
				iv = new IvParameterSpec(Arrays.copyOf(buffer, ivLength));
				cipher.init(Cipher.DECRYPT_MODE, encryptionKey, iv);

				// read metadata
				length = safeFile.readLong();
				byteBuffer.putLong(length);
				baos.reset();
				SafeIO.decrypt(safeFile, length, cipher, baos, buffer, bufferDecrypted);
				byteBuffer.put(baos.toByteArray());

				probe.checkCancel();

				// read data
				length = safeFile.readLong();
				byteBuffer.putLong(length);
				baos.reset();
				SafeIO.decrypt(safeFile, length, cipher, baos, buffer, bufferDecrypted);
				byteBuffer.put(baos.toByteArray());
			}

			safeFile.seek(previousPosition);

			return HASHER.hash(byteBuffer.array());

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
	 * Open a {@link Safe}
	 * 
	 * @param safeFilePath:
	 *            system path to the safe file
	 * @param password:
	 *            the encryption password
	 * @param bufferSize:size
	 *            of the <code>byte</code> buffer to be used in IO operation
	 * @param log
	 * @return
	 * @throws Exception
	 */
	public static Safe open(final String safeFilePath, final char [] password, final int bufferSize, final Logger log) throws Exception
	{
		final File file = new File(safeFilePath);

		if (!file.exists())
			throw new IOException("Safe file " + file + " doest not exist");

		final Map<String, String> header = Safe.readHeader(file, bufferSize);

		final String encyption = header.get(Safe.ENCRYPTION_LABEL);
		if (encyption == null)
			throw new Exception("Could not read property '" + Safe.ENCRYPTION_LABEL + "' from header");

		if (log != null)
			log.fine("Encryption type " + encyption);

		if (!header.containsKey(Safe.KEY_ALGO_LABEL))
			throw new Exception("Could not read property '" + Safe.KEY_ALGO_LABEL + "' from header");

		if (log != null)
			log.fine("Key algorithm " + header.get(Safe.KEY_ALGO_LABEL));

		final byte [] salt = (byte []) Safe.GSON.fromJson(header.get(Safe.PBKDF2_SALT_LABEL), Safe.BYTE_ARRAY_TYPE);

		PBEKeySpec spec = new PBEKeySpec(password, salt, Safe.PBKDF2_ITERATION, 128);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		final byte [] key = skf.generateSecret(spec).getEncoded();

		final SecretKeySpec keySpec = new SecretKeySpec(key, header.get(Safe.KEY_ALGO_LABEL));

		return new Safe(file, keySpec, bufferSize);
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

		if (!publicHeader.containsKey(ENCRYPTION_IV_LENGTH_LABEL))
			throw new Exception("Public property '" + ENCRYPTION_IV_LENGTH_LABEL + "' must be set");

		if (!publicHeader.containsKey(PBKDF2_SALT_LABEL))
			throw new Exception("Public property '" + PBKDF2_SALT_LABEL + "' must be set");

		if (!publicHeader.containsKey(PBKDF2_ITERATION_LABEL))
			throw new Exception("Public property '" + PBKDF2_ITERATION_LABEL + "' must be set");

		Cipher cipher = javax.crypto.Cipher.getInstance(encryption);

		final String keyAlgo = publicHeader.get(KEY_ALGO_LABEL);
		if (keyAlgo == null)
			throw new Exception("Public property '" + KEY_ALGO_LABEL + "' must be set");

		final SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgo);

		if (file.exists())
			throw new IOException("File " + file + " already exist");

		if (!file.createNewFile())
			throw new IOException("Could not create file " + file);

		final RandomAccessFile raf = new RandomAccessFile(file, "rw");

		final byte [] buffer = new byte[bufferSize];
		final byte [] bufferDecrypted = new byte[bufferSize];

		long total, position, previousPosition;

		cipher.init(Cipher.ENCRYPT_MODE, keySpec, getSecureRandom());

		raf.write(HASHER.getEmptyHash());// global hash

		// header
		position = raf.getFilePointer();
		// no IV in header
		raf.writeLong(0);
		final String header = GSON.toJson(publicHeader);
		total = SafeIO.write(new ByteArrayInputStream(header.getBytes(StandardCharsets.UTF_8)), raf, buffer, TaskProbe.DULL_PROBE);
		raf.writeLong(0);// no data in header block

		previousPosition = raf.getFilePointer();
		raf.seek(position);
		raf.writeLong(total);

		raf.seek(previousPosition);

		// properties
		position = raf.getFilePointer();
		raf.write(cipher.getIV());
		final String privatePropsJson = GSON.toJson(privateProperties == null ? new HashMap<>() : privateProperties);
		previousPosition = raf.getFilePointer();
		raf.writeLong(0L);
		total = SafeIO.encrypt(new ByteArrayInputStream(privatePropsJson.getBytes(StandardCharsets.UTF_8)), cipher, raf, buffer, bufferDecrypted, TaskProbe.DULL_PROBE);
		raf.writeLong(0);// no data in properties block

		raf.seek(previousPosition);
		raf.writeLong(total);

		// write global hash
		final byte [] hash = computeHash(raf, cipher, cipher.getIV().length, keySpec, bufferSize, null);
		raf.seek(0);
		raf.write(hash);

		raf.close();

		return new Safe(file, keySpec, bufferSize);
	}

}
