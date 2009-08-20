/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.hornetq.tests.unit.core.asyncio;

import org.hornetq.core.asyncio.AIOCallback;
import org.hornetq.core.asyncio.impl.AsynchronousFileImpl;
import org.hornetq.core.exception.MessagingException;
import org.hornetq.tests.util.UnitTestCase;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The base class for AIO Tests
 * @author Clebert Suconic
 *
 */
public abstract class AIOTestBase extends UnitTestCase
{
   // The AIO Test must use a local filesystem. Sometimes $HOME is on a NFS on
   // most enterprise systems

   protected String FILE_NAME = getTestDir() + "/fileUsedOnNativeTests.log";

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      File file = new File(getTestDir());

      deleteDirectory(file);

      file.mkdir();

      if (!AsynchronousFileImpl.isLoaded())
      {
         fail(String.format("libAIO is not loaded on %s %s %s",
                            System.getProperty("os.name"),
                            System.getProperty("os.arch"),
                            System.getProperty("os.version")));
      }
   }

   @Override
   protected void tearDown() throws Exception
   {
      assertEquals(0, AsynchronousFileImpl.getTotalMaxIO());

      super.tearDown();
   }

   protected void encodeBufer(final ByteBuffer buffer)
   {
      buffer.clear();
      int size = buffer.limit();
      for (int i = 0; i < size - 1; i++)
      {
         buffer.put((byte)('a' + i % 20));
      }

      buffer.put((byte)'\n');

   }

   protected void preAlloc(final AsynchronousFileImpl controller, final long size) throws MessagingException
   {
      controller.fill(0l, 1, size, (byte)0);
   }

   protected static class CountDownCallback implements AIOCallback
   {
      private final CountDownLatch latch;

      public CountDownCallback(final CountDownLatch latch)
      {
         this.latch = latch;
      }

      volatile boolean doneCalled = false;

      volatile boolean errorCalled = false;

      final AtomicInteger timesDoneCalled = new AtomicInteger(0);

      public void done()
      {
         doneCalled = true;
         timesDoneCalled.incrementAndGet();
         if (latch != null)
         {
            latch.countDown();
         }
      }

      public void onError(final int errorCode, final String errorMessage)
      {
         errorCalled = true;
         if (latch != null)
         {
            // even thought an error happened, we need to inform the latch,
               // or the test won't finish
            latch.countDown();
         }
      }
   }

}