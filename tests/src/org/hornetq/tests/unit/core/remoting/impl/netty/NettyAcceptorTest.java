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

package org.hornetq.tests.unit.core.remoting.impl.netty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.exception.MessagingException;
import org.hornetq.core.remoting.impl.AbstractBufferHandler;
import org.hornetq.core.remoting.spi.BufferHandler;
import org.hornetq.core.remoting.spi.Connection;
import org.hornetq.core.remoting.spi.ConnectionLifeCycleListener;
import org.hornetq.core.remoting.spi.MessagingBuffer;
import org.hornetq.integration.transports.netty.NettyAcceptor;
import org.hornetq.integration.transports.netty.TransportConstants;
import org.hornetq.tests.util.UnitTestCase;

/**
 *
 * A NettyAcceptorTest
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 */
public class NettyAcceptorTest extends UnitTestCase
{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      checkFreePort(TransportConstants.DEFAULT_PORT);      
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      checkFreePort(TransportConstants.DEFAULT_PORT);

      super.tearDown();
   }
   
   public void testStartStop() throws Exception
   {
      BufferHandler handler = new AbstractBufferHandler()
      {

         public void bufferReceived(Object connectionID, MessagingBuffer buffer)
         {
         }
      };

      Map<String, Object> params = new HashMap<String, Object>();
      ConnectionLifeCycleListener listener = new ConnectionLifeCycleListener()
      {

         public void connectionException(Object connectionID, MessagingException me)
         {
         }

         public void connectionDestroyed(Object connectionID)
         {
         }

         public void connectionCreated(Connection connection)
         {
         }
      };
      NettyAcceptor acceptor = new NettyAcceptor(params, handler, listener, 
                                                 Executors.newCachedThreadPool(), 
                                                 Executors.newScheduledThreadPool(ConfigurationImpl.DEFAULT_SCHEDULED_THREAD_POOL_MAX_SIZE));

      acceptor.start();
      assertTrue(acceptor.isStarted());
      acceptor.stop();
      assertFalse(acceptor.isStarted());      
      checkFreePort(TransportConstants.DEFAULT_PORT);
      
      acceptor.start();
      assertTrue(acceptor.isStarted());
      acceptor.stop();
      assertFalse(acceptor.isStarted());
      checkFreePort(TransportConstants.DEFAULT_PORT);
   }
     
}