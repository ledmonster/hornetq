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

import org.hornetq.core.client.ClientConsumer;
import org.hornetq.core.client.ClientMessage;
import org.hornetq.core.client.ClientProducer;
import org.hornetq.core.client.ClientSession;
import org.hornetq.core.client.ClientSessionFactory;
import org.hornetq.core.client.MessageHandler;
import org.hornetq.core.exception.MessagingException;
import org.hornetq.core.logging.Logger;
import org.hornetq.core.remoting.Interceptor;
import org.hornetq.core.remoting.Packet;
import org.hornetq.core.remoting.RemotingConnection;
import org.hornetq.core.remoting.impl.wireformat.PacketImpl;
import org.hornetq.core.server.MessagingServer;
import org.hornetq.core.server.Queue;
import org.hornetq.tests.util.ServiceTestBase;
import org.hornetq.utils.SimpleString;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 */
public class ConsumerTest extends ServiceTestBase
{
   private static final Logger log = Logger.getLogger(ConsumerTest.class);

   private MessagingServer server;

   private final SimpleString QUEUE = new SimpleString("ConsumerTestQueue");

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      server = createServer(false);

      server.start();
   }

   @Override
   protected void tearDown() throws Exception
   {
      server.stop();

      server = null;

      super.tearDown();
   }

   
  
   public void testConsumerAckImmediateAutoCommitTrue() throws Exception
   {
      ClientSessionFactory sf = createInVMFactory();

      ClientSession session = sf.createSession(false, true, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 100;

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = createTextMessage("m" + i, session);
         producer.send(message);
      }

      ClientConsumer consumer = session.createConsumer(QUEUE);
      session.start();
      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message2 = consumer.receive(1000);

         assertEquals("m" + i, message2.getBody().readString());
      }
      // assert that all the messages are there and none have been acked
      assertEquals(0,
                   ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getDeliveringCount());
      assertEquals(0,
                   ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getMessageCount());

      session.close();
   }

   public void testConsumerAckImmediateAutoCommitFalse() throws Exception
   {

      ClientSessionFactory sf = createInVMFactory();

      ClientSession session = sf.createSession(false, true, false, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 100;

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = createTextMessage("m" + i, session);
         producer.send(message);
      }

      ClientConsumer consumer = session.createConsumer(QUEUE);
      session.start();
      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message2 = consumer.receive(1000);

         assertEquals("m" + i, message2.getBody().readString());
      }
      // assert that all the messages are there and none have been acked
      assertEquals(0,
                   ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getDeliveringCount());
      assertEquals(0,
                   ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getMessageCount());

      session.close();
   }

   public void testConsumerAckImmediateAckIgnored() throws Exception
   {

      ClientSessionFactory sf = createInVMFactory();

      ClientSession session = sf.createSession(false, true, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 100;

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = createTextMessage("m" + i, session);
         producer.send(message);
      }

      ClientConsumer consumer = session.createConsumer(QUEUE);
      session.start();
      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message2 = consumer.receive(1000);

         assertEquals("m" + i, message2.getBody().readString());
         if (i < 50)
         {
            message2.acknowledge();
         }
      }
      // assert that all the messages are there and none have been acked
      assertEquals(0,
                   ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getDeliveringCount());
      assertEquals(0,
                   ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getMessageCount());

      session.close();
   }

   public void testConsumerAckImmediateCloseSession() throws Exception
   {

      ClientSessionFactory sf = createInVMFactory();

      ClientSession session = sf.createSession(false, true, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 100;

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = createTextMessage("m" + i, session);
         producer.send(message);
      }

      ClientConsumer consumer = session.createConsumer(QUEUE);
      session.start();
      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message2 = consumer.receive(1000);

         assertEquals("m" + i, message2.getBody().readString());
         if (i < 50)
         {
            message2.acknowledge();
         }
      }
      // assert that all the messages are there and none have been acked
      assertEquals(0,
                   ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getDeliveringCount());
      assertEquals(0,
                   ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getMessageCount());

      session.close();

      assertEquals(0,
                   ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getDeliveringCount());
      assertEquals(0,
                   ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getMessageCount());
   }

   public void testAcksWithSmallSendWindow() throws Exception
   {
      ClientSessionFactory sf = createInVMFactory();

      ClientSession session = sf.createSession(false, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 10000;

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = createTextMessage("m" + i, session);
         producer.send(message);
      }
      session.close();
      sf.close();
      final CountDownLatch latch = new CountDownLatch(numMessages);
      server.getRemotingService().addInterceptor(new Interceptor()
      {
         public boolean intercept(Packet packet, RemotingConnection connection) throws MessagingException
         {
            if(packet.getType() == PacketImpl.SESS_ACKNOWLEDGE)
            {
               latch.countDown();
            }
            return true;
         }
      });
      ClientSessionFactory sfReceive = createInVMFactory();
      sfReceive.setProducerWindowSize(100);
      sfReceive.setAckBatchSize(-1);
      ClientSession sessionRec = sfReceive.createSession(false, true, true);
      ClientConsumer consumer = sessionRec.createConsumer(QUEUE);
      consumer.setMessageHandler(new MessageHandler()
      {
         public void onMessage(ClientMessage message)
         {
            try
            {
               message.acknowledge();
            }
            catch (MessagingException e)
            {
               e.printStackTrace();
            }
         }
      });
      sessionRec.start();
      assertTrue(latch.await(5, TimeUnit.SECONDS));
      sessionRec.close();
   }
   
   public void testClearListener() throws Exception
   {
      ClientSessionFactory sf = createInVMFactory();

      ClientSession session = sf.createSession(false, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientConsumer consumer = session.createConsumer(QUEUE);
      
      consumer.setMessageHandler(new MessageHandler()
      {
         public void onMessage(ClientMessage msg)
         {
         }
      });

      consumer.setMessageHandler(null);
      consumer.receiveImmediate();
   }
   
   public void testNoReceiveWithListener() throws Exception
   {
      ClientSessionFactory sf = createInVMFactory();

      ClientSession session = sf.createSession(false, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientConsumer consumer = session.createConsumer(QUEUE);
      
      consumer.setMessageHandler(new MessageHandler()
      {
         public void onMessage(ClientMessage msg)
         {
         }
      });

      try
      {
         consumer.receiveImmediate();
         fail("Should throw exception");
      }
      catch (MessagingException me)
      {
         if (me.getCode() == MessagingException.ILLEGAL_STATE)
         {
            //Ok
         }
         else
         {
            fail("Wrong exception code");
         }
      }
   }

}