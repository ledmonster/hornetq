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


package org.hornetq.tests.integration.cluster.failover;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.hornetq.core.client.ClientConsumer;
import org.hornetq.core.client.ClientMessage;
import org.hornetq.core.client.ClientProducer;
import org.hornetq.core.client.ClientSession;
import org.hornetq.core.client.ClientSessionFactory;
import org.hornetq.core.client.impl.ClientSessionImpl;
import org.hornetq.core.client.impl.ClientSessionInternal;
import org.hornetq.core.exception.MessagingException;
import org.hornetq.core.logging.Logger;
import org.hornetq.core.paging.PagingManager;
import org.hornetq.core.paging.PagingStore;
import org.hornetq.core.remoting.RemotingConnection;
import org.hornetq.core.remoting.impl.RemotingConnectionImpl;
import org.hornetq.core.remoting.impl.invm.InVMConnector;
import org.hornetq.tests.util.RandomUtil;
import org.hornetq.utils.SimpleString;

/**
 * A PagingFailoverTest
 *
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 * 
 * Created Feb 5, 2009 10:57:42 AM
 *
 *
 */
public class PagingFailoverTest extends FailoverTestBase
{
   // Constants -----------------------------------------------------

   private final Logger log = Logger.getLogger(PagingFailoverTest.class);
  
   final int RECEIVE_TIMEOUT = 2000;

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   protected static final SimpleString ADDRESS = new SimpleString("FailoverTestAddress");

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   
   public void testMultithreadFailoverReplicationOnly() throws Throwable
   {
      setUpFileBased(getMaxGlobal(), getPageSize());

      int numberOfProducedMessages = multiThreadProducer(getNumberOfThreads(), false);

      System.out.println(numberOfProducedMessages + " messages produced");

      int numberOfConsumedMessages = multiThreadConsumer(getNumberOfThreads(), false, false);

      assertEquals(numberOfProducedMessages, numberOfConsumedMessages);
      
      System.out.println("Done!");

   }

   public void testMultithreadFailoverOnProducing() throws Throwable
   {
      setUpFileBased(getMaxGlobal(), getPageSize());
      
      int numberOfProducedMessages = multiThreadProducer(getNumberOfThreads(), true);

      System.out.println(numberOfProducedMessages + " messages produced");

      int numberOfConsumedMessages = multiThreadConsumer(getNumberOfThreads(), true, false);

      assertEquals(numberOfProducedMessages, numberOfConsumedMessages);
   }

   public void testMultithreadFailoverOnConsume() throws Throwable
   {
      setUpFileBased(getMaxGlobal(), getPageSize());

      int numberOfProducedMessages = multiThreadProducer(getNumberOfThreads(), false);

      System.out.println(numberOfProducedMessages + " messages produced");

      int numberOfConsumedMessages = multiThreadConsumer(getNumberOfThreads(), false, true);

      assertEquals(numberOfProducedMessages, numberOfConsumedMessages);

   }

   
   public void testFailoverOnPaging() throws Exception
   {
      testPaging(true);
   }

   public void testReplicationOnPaging() throws Exception
   {
      testPaging(false);
   }

   private void testPaging(final boolean fail) throws Exception
   {
      setUpFileBased(100 * 1024);

      ClientSession session = null;
      try
      {
         ClientSessionFactory sf1 = createFailoverFactory();
         
         sf1.setBlockOnAcknowledge(true);
         sf1.setBlockOnNonPersistentSend(true);
         sf1.setBlockOnPersistentSend(true);

         session = sf1.createSession(null, null, false, true, true, false, 0);

         session.createQueue(ADDRESS, ADDRESS, null, true);

         ClientProducer producer = session.createProducer(ADDRESS);

         final int numMessages = getNumberOfMessages();

         PagingManager pmLive = liveServer.getPostOffice().getPagingManager();
         PagingStore storeLive = pmLive.getPageStore(ADDRESS);

         PagingManager pmBackup = backupServer.getPostOffice().getPagingManager();
         PagingStore storeBackup = pmBackup.getPageStore(ADDRESS);

         for (int i = 0; i < numMessages; i++)
         {
            ClientMessage message = session.createClientMessage(true);
            message.getBody().writeInt(i);

            producer.send(message);

            if (storeLive.isPaging())
            {
               assertTrue(storeBackup.isPaging());
            }
         }

         session.close();
         session = sf1.createSession(null, null, false, true, true, false, 0);
         session.start();

         final RemotingConnection conn = ((ClientSessionInternal)session).getConnection();

         assertEquals("GloblSize", pmLive.getTotalMemory(), pmBackup.getTotalMemory());

         assertEquals("PageSizeLive", storeLive.getAddressSize(), pmLive.getTotalMemory());

         assertEquals("PageSizeBackup", storeBackup.getAddressSize(), pmBackup.getTotalMemory());

         ClientConsumer consumer = session.createConsumer(ADDRESS);

         for (int i = 0; i < numMessages; i++)
         {

            if (fail && i == numMessages / 2)
            {
               conn.fail(new MessagingException(MessagingException.NOT_CONNECTED));
            }

            ClientMessage message = consumer.receive(RECEIVE_TIMEOUT);


            assertNotNull(message);

            message.acknowledge();

            assertEquals(i, message.getBody().readInt());

         }

         session.close();
         session = null;

         if (!fail)
         {
            assertEquals(0, pmLive.getTotalMemory());
            assertEquals(0, storeLive.getAddressSize());
         }
         assertEquals(0, pmBackup.getTotalMemory());
         assertEquals(0, storeBackup.getAddressSize());

      }
      finally
      {
         if (session != null)
         {
            try
            {
               session.close();
            }
            catch (Exception ignored)
            {
               // eat it
            }
         }
      }

   }


   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
   
   protected int getNumberOfMessages()
   {
      return 500;
   }
   
   protected int getNumberOfThreads()
   {
      return 5;
   }
   
   protected int getMaxGlobal()
   {
      return 10024;
   }
   
   protected int getPageSize()
   {
      return 5120;
   }
   
   protected void fail(final ClientSession session) throws Exception
   {
      RemotingConnectionImpl conn = (RemotingConnectionImpl)((ClientSessionInternal)session).getConnection();
      System.out.println("Forcing a failure");
      conn.fail(new MessagingException(MessagingException.NOT_CONNECTED, "blah"));

   }


   // Private -------------------------------------------------------
   
   /**
    * @throws Exception
    * @throws InterruptedException
    * @throws Throwable
    */
   protected int multiThreadConsumer(int numberOfThreads, final boolean connectedOnBackup, final boolean fail) throws Exception,
                                                                                       InterruptedException,
                                                                                       Throwable
   {
      ClientSession session = null;
      try
      {
         final AtomicInteger numberOfMessages = new AtomicInteger(0);

         final ClientSessionFactory factory;
         final PagingStore store;

         if (connectedOnBackup)
         {
            factory = createBackupFactory();
            store = backupServer.getPostOffice().getPagingManager().getPageStore(ADDRESS);
         }
         else
         {
            factory = createFailoverFactory();
            store = liveServer.getPostOffice().getPagingManager().getPageStore(ADDRESS);
         }
         
         factory.setBlockOnNonPersistentSend(true);
         factory.setBlockOnAcknowledge(true);
         factory.setBlockOnPersistentSend(true);

         session = factory.createSession(false, true, true, false);

         final int initialNumberOfPages = store.getNumberOfPages();

         System.out.println("It has initially " + initialNumberOfPages);

         final CountDownLatch startFlag = new CountDownLatch(1);
         final CountDownLatch alignSemaphore = new CountDownLatch(numberOfThreads);

         class Consumer extends Thread
         {
            volatile Throwable e;

            ClientSession session;

            public Consumer() throws Exception
            {
               session = factory.createSession(null, null, false, true, true, false, 0);
            }

            @Override
            public void run()
            {
               boolean started = false;

               try
               {

                  try
                  {
                     ClientConsumer consumer = session.createConsumer(ADDRESS);

                     session.start();

                     alignSemaphore.countDown();

                     started = true;

                     startFlag.await();

                     while (true)
                     {
                        ClientMessage msg = consumer.receive(RECEIVE_TIMEOUT);
                        if (msg == null)
                        {
                           break;
                        }

                        if (numberOfMessages.incrementAndGet() % 1000 == 0)
                        {
                           System.out.println(numberOfMessages + " messages read");
                        }

                        msg.acknowledge();
                     }

                  }
                  finally
                  {
                     session.close();
                  }
               }
               catch (Throwable e)
               {
                  // Using System.out, as it would appear on the test output
                  e.printStackTrace(); 
                  if (!started)
                  {
                     alignSemaphore.countDown();
                  }
                  this.e = e;
               }
            }
         }

         Consumer[] consumers = new Consumer[numberOfThreads];

         for (int i = 0; i < numberOfThreads; i++)
         {
            consumers[i] = new Consumer();
         }

         for (int i = 0; i < numberOfThreads; i++)
         {
            consumers[i].start();
         }

         alignSemaphore.await();

         startFlag.countDown();

         if (fail)
         {
            // Fail after some time
            Thread.sleep((long)(1000 * RandomUtil.randomDouble()));
            while (store.getNumberOfPages() == initialNumberOfPages)
            {
               Thread.sleep(100);
            }

            System.out.println("The system has already depaged " + (initialNumberOfPages - store.getNumberOfPages()) +
                               ", failing now");

            fail(session);
         }

         for (Thread t : consumers)
         {
            t.join();
         }

         for (Consumer p : consumers)
         {
            if (p.e != null)
            {
               throw p.e;
            }
         }

         return numberOfMessages.intValue();
      }
      finally
      {
         if (session != null)
         {
            try
            {
               session.close();
            }
            catch (Exception ignored)
            {
            }
         }
      }
   }

   /**
    * @throws Exception
    * @throws MessagingException
    * @throws InterruptedException
    * @throws Throwable
    */
   protected int multiThreadProducer(final int numberOfThreads, final boolean failover) throws Exception,
                                                          MessagingException,
                                                          InterruptedException,
                                                          Throwable
   {

      final AtomicInteger numberOfMessages = new AtomicInteger(0);
      final PagingStore store = liveServer.getPostOffice().getPagingManager().getPageStore(ADDRESS);

      final ClientSessionFactory factory = createFailoverFactory();
      
      factory.setBlockOnNonPersistentSend(true);
      factory.setBlockOnAcknowledge(true);
      factory.setBlockOnPersistentSend(true);

      ClientSession session = factory.createSession(false, true, true, false);
      try
      {
         try
         {
            session.createQueue(ADDRESS, ADDRESS, null, true);
         }
         catch (Exception e)
         {          
         }

         final CountDownLatch startFlag = new CountDownLatch(1);
         final CountDownLatch alignSemaphore = new CountDownLatch(numberOfThreads);
         final CountDownLatch flagPaging = new CountDownLatch(numberOfThreads);

         class Producer extends Thread
         {
            volatile Throwable e;

            @Override
            public void run()
            {
               boolean started = false;
               try
               {
                  ClientSession session = factory.createSession(false, true, true);
                  try
                  {
                     ClientProducer producer = session.createProducer(ADDRESS);

                     alignSemaphore.countDown();

                     started = true;
                     startFlag.await();

                     while (!store.isPaging())
                     {

                        ClientMessage msg = session.createClientMessage(true);

                        producer.send(msg);
                        numberOfMessages.incrementAndGet();
                     }

                     flagPaging.countDown();

                     for (int i = 0; i < 100; i++)
                     {

                        ClientMessage msg = session.createClientMessage(true);

                        producer.send(msg);
                        numberOfMessages.incrementAndGet();

                     }

                  }
                  finally
                  {
                     session.close();
                  }
               }
               catch (Throwable e)
               {
                  // Using System.out, as it would appear on the test output
                  e.printStackTrace(); 
                  if (!started)
                  {
                     alignSemaphore.countDown();
                  }
                  flagPaging.countDown();
                  this.e = e;
               }
            }
         }

         Producer[] producers = new Producer[numberOfThreads];

         for (int i = 0; i < numberOfThreads; i++)
         {
            producers[i] = new Producer();
            producers[i].start();
         }

         alignSemaphore.await();

         // Start producing only when all the sessions are opened
         startFlag.countDown();

         if (failover)
         {
            flagPaging.await(); // for this test I want everybody on the paging part

            Thread.sleep(1500);

            fail(session);

         }

         for (Thread t : producers)
         {
            t.join();
         }

         for (Producer p : producers)
         {
            if (p.e != null)
            {
               throw p.e;
            }
         }

         return numberOfMessages.intValue();

      }
      finally
      {
         session.close();
         InVMConnector.resetFailures();
      }

   }
   

   // Inner classes -------------------------------------------------

}