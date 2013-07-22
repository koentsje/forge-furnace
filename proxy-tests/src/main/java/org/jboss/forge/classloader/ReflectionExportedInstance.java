/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.classloader;

import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.exception.ContainerException;
import org.jboss.forge.furnace.services.ExportedInstance;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class ReflectionExportedInstance<T> implements ExportedInstance<T>
{
   private Class<T> type;

   public ReflectionExportedInstance(Addon addon, Class<T> clazz)
   {
      this.type = clazz;
   }

   @Override
   public T get()
   {
      try
      {
         return type.newInstance();
      }
      catch (Exception e)
      {
         throw new ContainerException("Could not create instance of [" + type.getName() + "] through reflection.", e);
      }
   }

   @Override
   public void release(T instance)
   {
      // no action required
   }

}
