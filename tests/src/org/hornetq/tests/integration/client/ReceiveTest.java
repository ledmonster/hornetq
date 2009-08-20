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

import org.hornetq.core.client.ClientConsumer;
import org.hornetq.core.client.ClientMessage;
import org.hornetq.core.client.ClientProducer;
import org.hornetq.core.client.ClientSession;
import org.hornetq.core.client.ClientSessionFactory;
import org.hornetq.core.client.MessageHandler;
import org.hornetq.core.exception.MessagingException;
import org.hornetq.core.server.MessagingServer;
import org.hornetq.tests.util.ServiceTestBase;
import org.hornetq.utils.SimpleString;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class ReceiveTest extends ServiceTestBase
{
   SimpleString addressA = new SimpleString("addressA");

   SimpleString queueA = new SimpleString("queueA");

   public void testBasicReceive() throws Exception
   {
      MessagingServer server = createServer(false);
      try
      {
         server.start();
         ClientSessionFactory cf = createInVMFactory();
         ClientSession sendSession = cf.createSession(false, true, true);
         ClientProducer cp = sendSession.createProducer(addressA);
         ClientSession session = cf.createSession(false, true, true);
         session.createQueue(addressA, queueA, false);
         ClientConsumer cc = session.createConsumer(queueA);
         session.start();
         cp.send(sendSession.createClientMessage(false));
         assertNotNull(cc.receive());
         session.close();
         sendSession.close();
      }
      finally
      {
         if (server.isStarted())
         {
            server.stop();
         }
      }
   }

   public void testReceiveTimesoutCorrectly() throws Exception
   {
      MessagingServer server = createServer(false);
      try
      {
         server.start();
         ClientSessionFactory cf = createInVMFactory();
         ClientSession session = cf.createSession(false, true, true);
         session.createQueue(addressA, queueA, false);
         ClientConsumer cc = session.createConsumer(queueA);
         session.start();
         long time = System.currentTimeMillis();
         cc.receive(1000);
         assertTrue(System.currentTimeMillis() - time >= 1000);
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

   public void testReceiveOnClosedException() throws Exception
   {
      MessagingServer server = createServer(false);
      try
      {
         server.start();
         ClientSessionFactory cf = createInVMFactory();
         ClientSession session = cf.createSession(false, true, true);
         session.createQueue(addressA, queueA, false);
         ClientConsumer cc = session.createConsumer(queueA);
         session.start();
         session.close();
         try
         {
            cc.receive();
            fail("should throw exception");
         }
         catch (MessagingException e)
         {
            assertEquals(MessagingException.OBJECT_CLOSED, e.getCode());
         }
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

   public void testReceiveThrowsExceptionWhenHandlerSet() throws Exception
   {
      MessagingServer server = createServer(false);
      try
      {
         server.start();
         ClientSessionFactory cf = createInVMFactory();
         ClientSession session = cf.createSession(false, true, true);
         session.createQueue(addressA, queueA, false);
         ClientConsumer cc = session.createConsumer(queueA);
         session.start();
         cc.setMessageHandler(new MessageHandler()
         {
            public void onMessage(ClientMessage message)
            {
            }
         });
         try
         {
            cc.receive();
            fail("should throw exception");
         }
         catch (MessagingException e)
         {
            assertEquals(MessagingException.ILLEGAL_STATE, e.getCode());
         }
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

   public void testReceiveImmediate() throws Exception
   {
      MessagingServer server = createServer(false);
      try
      {
         server.start();
         ClientSessionFactory cf = createInVMFactory();
         //forces perfect round robin
         cf.setConsumerWindowSize(1);
         ClientSession sendSession = cf.createSession(false, true, true);
         ClientProducer cp = sendSession.createProducer(addressA);
         ClientSession session = cf.createSession(false, true, true);
         session.createQueue(addressA, queueA, false);
         ClientConsumer cc = session.createConsumer(queueA);
         ClientConsumer cc2 = session.createConsumer(queueA);
         session.start();
         cp.send(sendSession.createClientMessage(false));
         cp.send(sendSession.createClientMessage(false));
         cp.send(sendSession.createClientMessage(false));
         //at this point we know that the first consumer has a messge in ites buffer
         assertNotNull(cc2.receive(5000));
         assertNotNull(cc2.receive(5000));
         assertNotNull(cc.receiveImmediate());
         session.close();
         sendSession.close();
      }
      finally
      {
         if (server.isStarted())
         {
            server.stop();
         }
      }
   }
}