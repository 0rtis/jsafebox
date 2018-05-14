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

package org.ortis.jsafebox.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class MultipartTask implements Task
{

	private Task subTask;

	private double progress;
	private String message;

	private Exception exception;

	private boolean cancellationRequested;

	private boolean cancelled;

	private boolean terminated;

	private final List<TaskListener> listeners = new ArrayList<>();

	private AtomicBoolean silentSubTask = new AtomicBoolean(false);
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition event = lock.newCondition();
	private final Condition terminatedEvent = lock.newCondition();

	private final TaskListener listener = new TaskListener()
	{

		@Override
		public void onTerminated(Task task)
		{
		}

		@Override
		public void onProgress(final Task task, final double progress)
		{
			if (task != subTask)
				return;

			if (!silentSubTask.get())
				fireProgress(progress);
		}

		@Override
		public void onMessage(final Task task, final String message)
		{
			if (task != subTask)
				return;

			if (!silentSubTask.get())
				fireMessage(message);
		}

		@Override
		public void onException(final Task task, final Exception exception)
		{

			if (task != subTask)
				return;

			fireException(exception);
		}

		@Override
		public void onCancelled(final Task task)
		{

			if (task != subTask)
				return;

			fireCanceled();
		}

		@Override
		public void onCancellationRequested(final Task task)
		{
			if (task != subTask)
				return;

			cancel();
		}
	};

	protected void setSilentSubTask(final boolean silent)
	{
		this.silentSubTask.set(silent);
	}

	protected void setSubTask(final Task subTask)
	{
		this.lock.lock();
		try
		{
			if (this.subTask != null)
				this.subTask.removeListener(this.listener);

			this.subTask = subTask;

			if (this.subTask != null)
				this.subTask.addListener(this.listener);

		} finally
		{
			this.lock.unlock();
		}
	}

	protected void fireProgress(final double progress)
	{
		this.lock.lock();
		try
		{

			this.progress = progress;
			this.event.signalAll();

			for (final TaskListener listener : this.listeners)
				listener.onProgress(this, progress);

		} finally
		{
			this.lock.unlock();
		}

	}

	protected void fireMessage(final String message)
	{
		this.lock.lock();
		try
		{
			this.message = message;
			this.event.signalAll();

			for (final TaskListener listener : this.listeners)
				listener.onMessage(this, message);

		} finally
		{
			this.lock.unlock();
		}

	}

	protected void fireCanceled()
	{
		this.lock.lock();
		try
		{
			this.cancelled = true;
			this.event.signalAll();

			for (final TaskListener listener : this.listeners)
				listener.onCancelled(this);

		} finally
		{
			this.lock.unlock();
		}
	}

	protected void fireException(final Exception exception)
	{
		this.lock.lock();
		try
		{
			this.exception = exception;
			this.event.signalAll();

			for (final TaskListener listener : this.listeners)
				listener.onException(this, exception);
		} finally
		{
			this.lock.unlock();
		}
	}

	protected void fireTerminated()
	{
		this.lock.lock();
		try
		{
			this.terminated = true;
			this.event.signalAll();
			this.terminatedEvent.signalAll();

			for (final TaskListener listener : this.listeners)
				listener.onTerminated(this);

		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public double getProgress()
	{
		this.lock.lock();
		try
		{
			return this.progress;
		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public String getMessage()
	{
		this.lock.lock();
		try
		{
			return this.message;
		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public Exception getException()
	{
		this.lock.lock();
		try
		{
			return this.exception;
		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public boolean isTerminated()
	{
		this.lock.lock();
		try
		{
			return this.terminated;
		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public boolean isCompleted()
	{
		this.lock.lock();
		try
		{
			return this.terminated && this.exception == null;
		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public void cancel()
	{
		this.lock.lock();
		try
		{
			this.cancellationRequested = true;

			for (final TaskListener listener : this.listeners)
				listener.onCancellationRequested(this);

			if (this.subTask != null && !this.subTask.isCancelRequested())
				this.subTask.cancel();

		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public boolean isCancelRequested()
	{
		this.lock.lock();
		try
		{
			return this.cancellationRequested;

		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public boolean isCancelled()
	{
		this.lock.lock();
		try
		{
			return this.cancelled;
		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public void awaitUpdate() throws InterruptedException
	{
		this.lock.lock();
		try
		{
			this.event.await();

		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public boolean awaitUpdate(final long timeout, final TimeUnit unit) throws InterruptedException
	{

		this.lock.lock();
		try
		{
			return this.event.await(timeout, unit);

		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public void awaitTermination() throws InterruptedException
	{
		this.lock.lock();
		try
		{
			if (this.terminated)
				return;
			this.terminatedEvent.await();
		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException
	{

		this.lock.lock();
		try
		{
			if (this.terminated)
				return true;
			return this.terminatedEvent.await(timeout, unit);
		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public boolean addListener(final TaskListener listener)
	{
		this.lock.lock();
		try
		{
			if (this.listeners.contains(listener))
				return false;
			return this.listeners.add(listener);
		} finally
		{
			this.lock.unlock();
		}
	}

	@Override
	public boolean removeListener(final TaskListener listener)
	{
		this.lock.lock();
		try
		{
			return this.listeners.remove(listener);
		} finally
		{
			this.lock.unlock();
		}
	}

}
