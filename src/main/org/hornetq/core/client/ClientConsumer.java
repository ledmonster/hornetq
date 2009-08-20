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

package org.hornetq.core.client;

import org.hornetq.core.exception.MessagingException;

/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 * @author <a href="mailto:ataylor@redhat.com">Andy Taylor</a>
 */
public interface ClientConsumer
{
   ClientMessage receive() throws MessagingException;

   ClientMessage receive(long timeout) throws MessagingException;

   ClientMessage receiveImmediate() throws MessagingException;

   MessageHandler getMessageHandler() throws MessagingException;

   void setMessageHandler(MessageHandler handler) throws MessagingException;

   void close() throws MessagingException;
   
   boolean isClosed();   
   
   Exception getLastException();
}