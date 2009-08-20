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

package org.hornetq.jmstests.tools.container;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

import org.hornetq.core.logging.Logger;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision: 2868 $</tt>
 *
 * $Id: InVMInitialContextFactoryBuilder.java 2868 2007-07-10 20:22:16Z timfox $
 */
public class InVMInitialContextFactoryBuilder implements InitialContextFactoryBuilder
{
   // Constants ------------------------------------------------------------------------------------

   private static final Logger log = Logger.getLogger(InVMInitialContextFactoryBuilder.class);

   // Static ---------------------------------------------------------------------------------------

   // Attributes -----------------------------------------------------------------------------------

   // Constructors ---------------------------------------------------------------------------------

   public InVMInitialContextFactoryBuilder()
   {
   }

   // InitialContextFactoryBuilder implementation --------------------------------------------------

   public InitialContextFactory createInitialContextFactory(Hashtable environment)
         throws NamingException
   {

      InitialContextFactory icf = null;

      if (environment != null)
      {
         String icfName = (String)environment.get("java.naming.factory.initial");

         if (icfName != null)
         {
            Class c = null;

            try
            {
               c = Class.forName(icfName);
            }
            catch(ClassNotFoundException e)
            {
               log.error("\"" + icfName + "\" cannot be loaded", e);
               throw new NamingException("\"" + icfName + "\" cannot be loaded");
            }

            try
            {
               icf = (InitialContextFactory)c.newInstance();
            }
            catch(InstantiationException e)
            {
               log.error(c.getName() + " cannot be instantiated", e);
               throw new NamingException(c.getName() + " cannot be instantiated");
            }
            catch(IllegalAccessException e)
            {
               log.error(c.getName() + " instantiation generated an IllegalAccessException", e);
               throw new NamingException(c.getName() +
                  " instantiation generated an IllegalAccessException");
            }
         }
      }

      if (icf == null)
      {
         icf = new InVMInitialContextFactory();
      }

      return icf;
   }

   // Public ---------------------------------------------------------------------------------------

   // Package protected ----------------------------------------------------------------------------

   // Protected ------------------------------------------------------------------------------------

   // Private --------------------------------------------------------------------------------------

   // Inner classes --------------------------------------------------------------------------------

}