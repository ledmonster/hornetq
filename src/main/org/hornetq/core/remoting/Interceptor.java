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

package org.hornetq.core.remoting;

import org.hornetq.core.exception.MessagingException;

/**
 *
 * This is class is a simple way to intercepting server calls on JBoss Messaging.
 * 
 * To Add this interceptor, you have to modify jbm-configuration.xml
 * 
 * @author clebert.suconic@jboss.com
 * @author tim.fox@jboss.com
 */
public interface Interceptor
{   
   /**
    * 
    * @param packet
    * @param connection
    * @return true to process the next interceptor and handle the packet,
    *         false to abort processing of the packet
    * @throws MessagingException
    */
   boolean intercept(Packet packet, RemotingConnection connection) throws MessagingException;
}