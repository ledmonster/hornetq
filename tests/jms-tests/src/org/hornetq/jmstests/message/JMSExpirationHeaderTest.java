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

package org.hornetq.jmstests.message;

import java.util.concurrent.CountDownLatch;

import javax.jms.DeliveryMode;
import javax.jms.Message;

import org.hornetq.jms.client.HornetQMessage;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class JMSExpirationHeaderTest extends MessageHeaderTestBase
{
   // Constants -----------------------------------------------------

   // Static --------------------------------------------------------
   
   // Attributes ----------------------------------------------------

   private volatile boolean testFailed;
   private volatile long effectiveReceiveTime;
   private volatile Message expectedMessage;

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void setUp() throws Exception
   {
      super.setUp();
      expectedMessage = null;
      testFailed = false;
      effectiveReceiveTime = 0;
   }

   public void tearDown() throws Exception
   {
      super.tearDown();
   }

   // Tests ---------------------------------------------------------

   public void testZeroExpiration() throws Exception
   {
      Message m = queueProducerSession.createMessage();
      queueProducer.send(m);
      assertEquals(0, queueConsumer.receive().getJMSExpiration());
   }

   public void testNoExpirationOnTimeoutReceive() throws Exception
   {
      Message m = queueProducerSession.createMessage();
      queueProducer.send(m, DeliveryMode.NON_PERSISTENT, 4, 5000);
      
      //DeliveryImpl is asynch - need to give enough time to get to the consumer
      Thread.sleep(2000);
      
      Message result = queueConsumer.receive(10);
      assertEquals(m.getJMSMessageID(), result.getJMSMessageID());
   }

   public void testExpirationOnTimeoutReceive() throws Exception
   {
      Message m = queueProducerSession.createMessage();
      queueProducer.send(m, DeliveryMode.NON_PERSISTENT, 4, 1000);
      
      // DeliveryImpl is asynch - need to give enough time to get to the consumer
      Thread.sleep(2000);
      
      assertNull(queueConsumer.receive(100));
   }

   public void testExpirationOnReceiveNoWait() throws Exception
   {
      Message m = queueProducerSession.createMessage();
      queueProducer.send(m, DeliveryMode.NON_PERSISTENT, 4, 1000);
      
      // DeliveryImpl is asynch - need to give enough time to get to the consumer
      Thread.sleep(2000);
      
      assertNull(queueConsumer.receiveNoWait());
   }

   public void testExpiredMessageDiscardingOnTimeoutReceive() throws Exception
   {
      Message m = queueProducerSession.createMessage();
      queueProducer.send(m, DeliveryMode.NON_PERSISTENT, 4, 1000);
      
      // DeliveryImpl is asynch - need to give enough time to get to the consumer
      Thread.sleep(2000);

      // start the receiver thread
      final CountDownLatch latch = new CountDownLatch(1);
      Thread receiverThread = new Thread(new Runnable()
      {
         public void run()
         {
            try
            {
               expectedMessage = queueConsumer.receive(100);
            }
            catch(Exception e)
            {
               log.trace("receive() exits with an exception", e);
            }
            finally
            {
               latch.countDown();
            }
         }
      }, "receiver thread");
      receiverThread.start();

      latch.await();
      assertNull(expectedMessage);
   }

   public void testReceiveTimeoutPreservation() throws Exception
   {
      final long timeToWaitForReceive = 5000;

      final CountDownLatch receiverLatch = new CountDownLatch(1);

      // start the receiver thread
      Thread receiverThread = new Thread(new Runnable()
      {
         public void run()
         {
            try
            {
               long t1 = System.currentTimeMillis();
               expectedMessage = queueConsumer.receive(timeToWaitForReceive);
               effectiveReceiveTime = System.currentTimeMillis() - t1;
            }
            catch(Exception e)
            {
               log.trace("receive() exits with an exception", e);
            }
            finally
            {
               receiverLatch.countDown();
            }
         }
      }, "receiver thread");
      receiverThread.start();

      final CountDownLatch senderLatch = new CountDownLatch(1);

      // start the sender thread
      Thread senderThread = new Thread(new Runnable()
      {
         public void run()
         {
            try
            {
               // wait for 3 secs
               Thread.sleep(3000);

               // send an expired message
               Message m = queueProducerSession.createMessage();
               queueProducer.send(m, DeliveryMode.NON_PERSISTENT, 4, -1);

               HornetQMessage jbm = (HornetQMessage)m;
               
               if (!jbm.getCoreMessage().isExpired())
               {
                  log.error("The message " + m + " should have expired");
                  testFailed = true;
                  return;
               }
            }
            catch(Exception e)
            {
               log.error("This exception will fail the test", e);
               testFailed = true;
            }
            finally
            {
               senderLatch.countDown();
            }
         }
      }, "sender thread");
      senderThread.start();


      senderLatch.await();
      receiverLatch.await();

      if (testFailed)
      {
         fail("Test failed by the sender thread. Watch for exception in logs");
      }

      log.trace("planned waiting time: " + timeToWaitForReceive +
                " effective waiting time " + effectiveReceiveTime);
      assertTrue(effectiveReceiveTime >= timeToWaitForReceive);
      assertTrue(effectiveReceiveTime < timeToWaitForReceive * 1.5);  // well, how exactly I did come
                                                                      // up with this coeficient is
                                                                      // not clear even to me, I just
                                                                      // noticed that if I use 1.01
                                                                      // this assertion sometimes
                                                                      // fails;
           
      assertNull(expectedMessage);
   }


   public void testNoExpirationOnReceive() throws Exception
   {
      Message m = queueProducerSession.createMessage();
      queueProducer.send(m, DeliveryMode.NON_PERSISTENT, 4, 5000);
      Message result = queueConsumer.receive();
      assertEquals(m.getJMSMessageID(), result.getJMSMessageID());
   }



   public void testExpirationOnReceive() throws Exception
   {
      expectedMessage = new HornetQMessage();

      queueProducer.send(queueProducerSession.createMessage(), DeliveryMode.NON_PERSISTENT, 4, 2000);

      // allow the message to expire
      Thread.sleep(3000);
      
      //When a consumer is closed while a receive() is in progress it will make the
      //receive return with null

      final CountDownLatch latch = new CountDownLatch(1);
      // blocking read for a while to make sure I don't get anything, not even a null
      Thread receiverThread = new Thread(new Runnable()
      {
         public void run()
         {
            try
            {
               log.trace("Attempting to receive");
               expectedMessage = queueConsumer.receive();
               
               //NOTE on close, the receive() call will return with null
               log.trace("Receive exited without exception:" + expectedMessage);
            }
            catch(Exception e)
            {
               log.trace("receive() exits with an exception", e);
               fail();
            }
            catch(Throwable t)
            {
               log.trace("receive() exits with an throwable", t);
               fail();
            }
            finally
            {
               latch.countDown();
            }
         }
      }, "receiver thread");
      receiverThread.start();

      Thread.sleep(3000);
      //receiverThread.interrupt();
      
      queueConsumer.close();

      // wait for the reading thread to conclude
      latch.await();

      log.trace("Expected message:" + expectedMessage);
      
      assertNull(expectedMessage);      
   }

   /*
    * Need to make sure that expired messages are acked so they get removed from the
    * queue/subscription, when delivery is attempted
    */
   public void testExpiredMessageDoesNotGoBackOnQueue() throws Exception
   {
      Message m = queueProducerSession.createMessage();
      
      m.setStringProperty("weebles", "wobble but they don't fall down");
      
      queueProducer.send(m, DeliveryMode.NON_PERSISTENT, 4, 1000);
      
      //DeliveryImpl is asynch - need to give enough time to get to the consumer
      Thread.sleep(2000);
      
      assertNull(queueConsumer.receive(100));
      
      //Need to check message isn't still in queue
      
      checkEmpty(queue1);           
   }


   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}