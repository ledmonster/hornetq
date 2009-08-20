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

package org.hornetq.tests.integration.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.hornetq.core.client.ClientConsumer;
import org.hornetq.core.client.ClientMessage;
import org.hornetq.core.client.ClientProducer;
import org.hornetq.core.client.ClientSession;
import org.hornetq.core.client.ClientSessionFactory;
import org.hornetq.core.client.MessageHandler;
import org.hornetq.core.server.MessagingServer;
import org.hornetq.tests.util.ServiceTestBase;
import org.hornetq.utils.SimpleString;

/**
 * A MessageRateTest
 *
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 *
 *
 */
public class MessageRateTest extends ServiceTestBase
{

   // Constants -----------------------------------------------------

   private final SimpleString ADDRESS = new SimpleString("ADDRESS");

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void testProduceRate() throws Exception
   {
      MessagingServer server = createServer(false);

      try
      {
         server.start();

         ClientSessionFactory sf = createInVMFactory();
         sf.setProducerMaxRate(10);
         ClientSession session = sf.createSession(false, true, true);
         
         session.createQueue(ADDRESS, ADDRESS, true);

         ClientProducer producer = session.createProducer(ADDRESS);
         long start = System.currentTimeMillis();
         for (int i = 0; i < 10; i++)
         {
            producer.send(session.createClientMessage(false));
         }
         long end = System.currentTimeMillis();

         assertTrue("TotalTime = " + (end - start), end - start >= 1000);
         
         session.close();
      }
      finally
      {
         if (server.isStarted())
         {
            server.stop();
         }
      }

   }


   public void testConsumeRate() throws Exception
   {
      MessagingServer server = createServer(false);

      try
      {
         server.start();

         ClientSessionFactory sf = createInVMFactory();
         sf.setConsumerMaxRate(10);
         
         ClientSession session = sf.createSession(false, true, true);
         
         session.createQueue(ADDRESS, ADDRESS, true);


         ClientProducer producer = session.createProducer(ADDRESS);
         
         for (int i = 0; i < 12; i++)
         {
            producer.send(session.createClientMessage(false));
         }

         session.start();
         
         ClientConsumer consumer = session.createConsumer(ADDRESS);

         long start = System.currentTimeMillis();
         
         for (int i = 0; i < 12; i++)
         {
            consumer.receive(1000);
         }
         
         long end = System.currentTimeMillis();

         assertTrue("TotalTime = " + (end - start), end - start >= 1000);
         
         session.close();
      }
      finally
      {
         if (server.isStarted())
         {
            server.stop();
         }
      }

   }


   public void testConsumeRateListener() throws Exception
   {
      MessagingServer server = createServer(false);

      try
      {
         server.start();

         ClientSessionFactory sf = createInVMFactory();
         sf.setConsumerMaxRate(10);
         
         ClientSession session = sf.createSession(false, true, true);
         
         session.createQueue(ADDRESS, ADDRESS, true);


         ClientProducer producer = session.createProducer(ADDRESS);
         
         for (int i = 0; i < 12; i++)
         {
            producer.send(session.createClientMessage(false));
         }
         
         ClientConsumer consumer = session.createConsumer(ADDRESS);
         
         final AtomicInteger failures = new AtomicInteger(0);
         
         final CountDownLatch messages = new CountDownLatch(12);
         
         consumer.setMessageHandler(new MessageHandler()
         {

            public void onMessage(ClientMessage message)
            {
               try
               {
                  message.acknowledge();
                  messages.countDown();
               }
               catch (Exception e)
               {
                  e.printStackTrace(); // Hudson report
                  failures.incrementAndGet();
               }
            }
            
         });

         
         long start = System.currentTimeMillis();
         session.start();
         assertTrue(messages.await(5, TimeUnit.SECONDS));
         long end = System.currentTimeMillis();
         
         assertTrue("TotalTime = " + (end - start), end - start >= 1000);
         
         session.close();
      }
      finally
      {
         if (server.isStarted())
         {
            server.stop();
         }
      }

   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   @Override
   protected void tearDown() throws Exception
   {
      super.tearDown();
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}