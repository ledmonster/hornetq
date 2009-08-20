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

package org.hornetq.core.journal.impl;

import java.util.concurrent.CountDownLatch;

import org.hornetq.core.exception.MessagingException;
import org.hornetq.core.journal.IOCallback;
import org.hornetq.core.logging.Logger;

/**
 * A SimpleWaitIOCallback
 *
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 *
 *
 */
public class SimpleWaitIOCallback implements IOCallback
{

   private static final Logger log = Logger.getLogger(SimpleWaitIOCallback.class);

   private final CountDownLatch latch = new CountDownLatch(1);

   private volatile String errorMessage;

   private volatile int errorCode = 0;

   public static IOCallback getInstance()
   {
      return new SimpleWaitIOCallback();
   }


   public void done()
   {
      latch.countDown();
   }

   public void onError(final int errorCode, final String errorMessage)
   {
      this.errorCode = errorCode;

      this.errorMessage = errorMessage;

      log.warn("Error Message " + errorMessage);

      latch.countDown();
   }

   public void waitCompletion() throws Exception
   {
      latch.await();
      if (errorMessage != null)
      {
         throw new MessagingException(errorCode, errorMessage);
      }
      return;
   }
}