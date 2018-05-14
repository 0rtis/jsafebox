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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Block implements SafeFile
{

	public final static String PATH_LABEL = "id";
	public final static String NAME_LABEL = "name";

	private final String path;
	private final String comparablePath;
	private final long offset;
	private final long length;
	private final long metaOffset;
	private final long metaLength;
	private final long dataOffset;
	private final long dataLength;

	private final Map<String, String> properties;

	private final Folder parent;

	public Block(final String path, final Map<String, String> properties, final long offset, final long length, final long metaOffset, final long metaLength, final long dataOffset,
			final long dataLength, final Folder parent)
	{
		this.path = path;
		this.comparablePath = path.toUpperCase(Environment.getLocale());
		this.offset = offset;
		this.length = length;
		this.metaOffset = metaOffset;
		this.metaLength = metaLength;
		this.dataOffset = dataOffset;
		this.dataLength = dataLength;

		final Map<String, String> props = new LinkedHashMap<>();
		props.putAll(properties);

		final String name = SafeFiles.getName(path);

		if (name == null)
			throw new IllegalArgumentException("Path '" + path + "' is not valid");

		props.put(PATH_LABEL, path);
		props.put(NAME_LABEL, name);

		this.properties = Collections.unmodifiableMap(props);

		this.parent = parent;
	}

	@Override
	public boolean isBlock()
	{

		return true;
	}

	@Override
	public boolean isFolder()
	{

		return false;
	}

	public String getComparablePath()
	{
		return comparablePath;
	}

	public String getPath()
	{
		return path;
	}

	public long getOffset()
	{
		return offset;
	}

	public long getLength()
	{
		return length;
	}

	public Map<String, String> getProperties()
	{
		return properties;
	}

	public String getName()
	{
		return this.properties.get(NAME_LABEL);

	}

	public String getComparableName()
	{
		return this.properties.get(NAME_LABEL).toUpperCase();

	}

	public long getMetaOffset()
	{
		return metaOffset;
	}

	public long getMetaLength()
	{
		return metaLength;
	}

	public long getDataOffset()
	{
		return dataOffset;
	}

	public long getDataLength()
	{
		return dataLength;
	}

	@Override
	public Folder getParent()
	{
		return this.parent;
	}

	@Override
	public int hashCode()
	{
		return this.comparablePath.hashCode();
	}

	@Override
	public boolean equals(final Object o)
	{
		if (o == this)
			return true;

		if (o instanceof Block)
			return this.comparablePath.equals(((Block) o).comparablePath);

		return false;
	}

	@Override
	public String toString()
	{

		return this.properties.toString();
	}

}
