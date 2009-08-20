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

package org.hornetq.core.server;

import org.hornetq.core.journal.EncodingSupport;
import org.hornetq.core.message.Message;

/**
 * 
 * A ServerMessage
 * 
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 *
 */
public interface ServerMessage extends Message, EncodingSupport
{
   void setMessageID(long id);

   MessageReference createReference(Queue queue);

   int incrementRefCount();

   int incrementDurableRefCount();

   int decrementDurableRefCount();

   int decrementRefCount();

   ServerMessage copy(long newID) throws Exception;
   
   ServerMessage copy() throws Exception;

   int getMemoryEstimate();

   void setStored();

   boolean isStored();

   int getRefCount();
   
   //TODO - we might be able to put this in a better place
}